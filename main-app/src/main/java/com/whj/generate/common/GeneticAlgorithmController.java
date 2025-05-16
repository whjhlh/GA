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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


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



    /**
     * 初始化环境，返回 sessionId 和初始种群信息。
     */
    @PostMapping("/init")
    @ResponseBody
    public InitResponse init(@RequestBody AlgorithmRequest request) {
        System.out.println("POST init 被调用");
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
        System.out.println("POST evolve 被调用");
        Nature nature = checkAndGetNature(request.getSessionId());

        int genIndex = request.getGenerationIndex();
        if (genIndex < 0 ) {
            throw new IllegalArgumentException("无效的参数");
        }
        // 执行一次进化
        Population nextPop = geneticAlgorithmService.evolvePopulation(nature, genIndex);
        boolean finished = isPopulationFinished(nextPop, genIndex);

        return ChromosomeConvertor.getEvolveResponse(request, genIndex, nextPop, finished);
    }

    private static boolean isPopulationFinished(Population nextPop, int genIndex) {
        return nextPop.getCurrentCoverage() >= GeneticAlgorithmConfig.TARGET_COVERAGE
                || genIndex + 1 >= GeneticAlgorithmConfig.MAX_GENERATION_COUNT;
    }


    @GetMapping("/generation-details")
    @ResponseBody
    public PopulationResponse getPopulation(@RequestParam String sessionId,
                                            @RequestParam int generationIndex) {
        System.out.println("GET population 被调用");
        Nature nature = checkAndGetNature(sessionId);
        if (generationIndex < 0 || generationIndex >= nature.getPopulationList().size()) {
            throw new IllegalArgumentException("无效的参数");
        }
        Population pop = nature.getPopulationList().get(generationIndex);
        Map<Chromosome, Integer> chromosomeSequenceMap = getChromosomeSequenceMap();

        return ChromosomeConvertor.getPopulationResponse(sessionId, generationIndex, pop, chromosomeSequenceMap);
    }

    /**
     * 获取种群可视化数据（多目标指标随代数的变化）
     */
    @GetMapping("/visualization")
    @ResponseBody
    public List<Map<String, Object>> getPopulationVisualization(@RequestParam String sessionId) {
        System.out.println("GET population-visualization 被调用");
        Nature nature = checkAndGetNature(sessionId);

        ChromosomeCoverageTracker tracker = geneticAlgorithmService.getCoverageTracker();
        List<Population> populations = nature.getPopulationList();

        return buildVisualResult(populations, tracker);
    }

    private Nature checkAndGetNature(String sessionId) {
        Nature nature = sessions.get(sessionId);
        if (nature == null || nature.getPopulationList().isEmpty()) {
            throw new IllegalArgumentException("无效的 sessionId 或种群数据为空");
        }
        return nature;
    }
    private static List<Map<String, Object>> buildVisualResult(List<Population> populations, ChromosomeCoverageTracker tracker) {
        List<Map<String, Object>> result = new ArrayList<>();
        double previousCoverage = 0.0;

        for (int i = 0; i < populations.size(); i++) {
            Population pop = populations.get(i);

            double coverage = pop.getCurrentCoverage();
            double deltaCoverage = coverage - previousCoverage;
            previousCoverage = coverage;

            double similarity = tracker.getSimilarityAtGeneration(pop);

            Map<String, Object> dataPoint = new HashMap<>();
            dataPoint.put("generation", i);
            dataPoint.put("coverage", coverage);
            dataPoint.put("deltaCoverage", deltaCoverage);
            dataPoint.put("similarity", similarity);

            result.add(dataPoint);
        }
        return result;
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

    // 构造函数注入
    public GeneticAlgorithmController(GeneticAlgorithmService geneticAlgorithmService) {
        this.geneticAlgorithmService = geneticAlgorithmService;
    }
}