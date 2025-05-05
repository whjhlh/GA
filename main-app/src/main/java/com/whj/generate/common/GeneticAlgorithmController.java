package com.whj.generate.common;


import com.whj.generate.biz.Infrastructure.cache.ChromosomeCoverageTracker;
import com.whj.generate.common.config.GeneticAlgorithmConfig;
import com.whj.generate.common.convert.ChromosomeConvertor;
import com.whj.generate.common.req.AlgorithmRequest;
import com.whj.generate.common.req.EvolveRequest;
import com.whj.generate.common.req.InitResponse;
import com.whj.generate.common.response.EvolveResponse;
import com.whj.generate.common.response.PopulationResponse;
import com.whj.generate.core.domain.Chromosome;
import com.whj.generate.core.domain.Nature;
import com.whj.generate.core.domain.Population;
import com.whj.generate.core.service.GeneticAlgorithmService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.whj.generate.utill.FileUtil.reportedInFile;

/**
 * @author whj
 * @date 2025-04-10 上午2:46
 */
@Controller

@RequestMapping("/api/genetic-algorithm")
public class GeneticAlgorithmController {
    /**
     * 封装 GeneticAlgorithmService
     */
    private final GeneticAlgorithmService geneticAlgorithmService;
    /**
     * 会话存储：sessionId -> Nature
     */
    private final Map<String, Nature> sessions = new ConcurrentHashMap<>();


    // 构造函数注入
    public GeneticAlgorithmController(GeneticAlgorithmService geneticAlgorithmService) {
        this.geneticAlgorithmService = geneticAlgorithmService;
    }

    /**
     * 初始化环境，返回 sessionId 和初始种群信息。
     */
    @PostMapping("/init")
    @ResponseBody
    public InitResponse init(@RequestBody AlgorithmRequest request) {
        try {
            Class<?> targetClass = Class.forName("com.whj.generate.whjtest." + request.getClassName());
            Nature nature = new Nature();
            // 初始化环境
            Population initialPop = geneticAlgorithmService.initEnvironment(nature, targetClass, request.getMethodName());
            String sessionId = getSessionId(nature);

            // 构造响应
            return ChromosomeConvertor.getInitResponse(sessionId, initialPop);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("未找到类: " + request.getClassName(), e);
        } catch (Exception e) {
            throw new RuntimeException("初始化失败: " + e.getMessage());
        }
    }

    /**
     * 执行一次进化（按指定 sessionId），返回新的种群信息。
     */
    @PostMapping("/evolve")
    @ResponseBody
    public EvolveResponse evolve(@RequestBody EvolveRequest request) {
        Nature nature = sessions.get(request.getSessionId());
        if (nature == null) {
            throw new IllegalArgumentException("无效的 sessionId: " + request.getSessionId());
        }
        int genIndex = request.getGenerationIndex();
        // 执行一次进化
        Population nextPop = geneticAlgorithmService.evolvePopulation(nature, genIndex);
        boolean finished = nextPop.getCurrentCoverage() >= GeneticAlgorithmConfig.TARGET_COVERAGE
                || genIndex + 1 >= GeneticAlgorithmConfig.MAX_GENERATION_COUNT;

        return ChromosomeConvertor.getEvolveResponse(request, genIndex, nextPop, finished);
    }

    /**
     * 改造后的算法入口（改为接收动态参数）
     *
     * @param targetClass  目标类
     * @param targetMethod 目标方法
     */
    @Async
    protected void runGeneticAlgorithm(Class<?> targetClass, String targetMethod) {
        final Nature nature = new Nature();

        // 初始化环境时传入目标方法
        initializeEnvironment(nature, targetClass, targetMethod);
        // 执行进化过程
        performEvolution(nature);
    }

    private void initializeEnvironment(Nature nature, Class<?> targetClass, String phaseName) {
        long startTime = System.nanoTime();
        Population population = geneticAlgorithmService.initEnvironment(nature, targetClass, phaseName);
        logOperationDuration(startTime, population, GeneticAlgorithmConfig.INIT_PHASE);
    }

    /**
     * 执行进化过程
     *
     * @param nature
     */
    private void performEvolution(Nature nature) {
        int generationCount = 0;
        Population currentPopulation = nature.getPopulationList().iterator().next();

        while (shouldContinueEvolution(currentPopulation, generationCount)) {
            long startTime = System.nanoTime();

            currentPopulation = geneticAlgorithmService.evolvePopulation(nature, generationCount);
            logOperationDuration(startTime, currentPopulation, String.valueOf(generationCount));
            generationCount++;
        }
    }

    private boolean shouldContinueEvolution(Population population, int currentGeneration) {
        return population.getCurrentCoverage() < GeneticAlgorithmConfig.TARGET_COVERAGE
                && currentGeneration < GeneticAlgorithmConfig.MAX_GENERATION_COUNT;
    }

    private void logOperationDuration(long startTime, Population population, String phase) {
        long duration = System.nanoTime() - startTime;
        ChromosomeCoverageTracker coverageTracker = geneticAlgorithmService.getCoverageTracker();
        reportedInFile(duration, population, phase, coverageTracker);
    }

    @GetMapping("/population")
    @ResponseBody
    public PopulationResponse getPopulation(@RequestParam String sessionId,
                                            @RequestParam int generationIndex) {
        Nature nature = sessions.get(sessionId);
        if (nature == null || generationIndex < 0 || generationIndex >= nature.getPopulationList().size()) {
            throw new IllegalArgumentException("无效的参数");
        }
        Population pop = nature.getPopulationList().get(generationIndex);
        Map<Chromosome, Integer> chromosomeSequenceMap = getChromosomeSequenceMap();

        return ChromosomeConvertor.getPopulationResponse(sessionId, generationIndex, pop, chromosomeSequenceMap);
    }

    private Map<Chromosome, Integer> getChromosomeSequenceMap() {
        ChromosomeCoverageTracker coverageTracker = geneticAlgorithmService.getCoverageTracker();
        return coverageTracker.getChromosomeSequenceMap();
    }

    private String getSessionId(Nature nature) {
        // 生成 sessionId 并保存状态
        String sessionId = UUID.randomUUID().toString();
        sessions.put(sessionId, nature);
        return sessionId;
    }
}