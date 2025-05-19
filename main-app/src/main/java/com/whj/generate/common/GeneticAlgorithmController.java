package com.whj.generate.common;


import com.whj.generate.biz.Infrastructure.SessionManager;
import com.whj.generate.biz.Infrastructure.cache.ChromosomeCoverageTracker;
import com.whj.generate.common.config.GeneticAlgorithmConfig;
import com.whj.generate.common.convert.ChromosomeConvertor;
import com.whj.generate.common.dto.CoveredDTO;
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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;


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
    private final ChromosomeCoverageTracker coverageTracker;
    private final SessionManager sessionManager;  // 新增会话管理组件

    @Autowired
    public GeneticAlgorithmController(GeneticAlgorithmService geneticAlgorithmService,
                                      ChromosomeCoverageTracker coverageTracker,
                                      SessionManager sessionManager) {
        this.geneticAlgorithmService = geneticAlgorithmService;
        this.coverageTracker = coverageTracker;
        this.sessionManager = sessionManager;
    }

    /**
     * 初始化环境，返回 sessionId 和初始种群信息。
     */
    @PostMapping("/init")
    @ResponseBody
    public InitResponse init(@RequestBody AlgorithmRequest request) throws ClassNotFoundException {
        System.out.println("POST init 被调用");

            initTracker();
            Class<?> targetClass = Class.forName("com.whj.generate.whjtest." + request.getClassName());
            Nature nature = new Nature();
            // 初始化环境
            Population initialPop = geneticAlgorithmService.initEnvironment(nature, targetClass, request.getMethodName());
            String sessionId = sessionManager.createSession(nature);

            // 构造响应
            return ChromosomeConvertor.getInitResponse(sessionId, initialPop);
    }

    private void initTracker() {
        coverageTracker.clear();
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
        if (genIndex < 0) {
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

        List<Population> populations = nature.getPopulationList();

        return buildVisualResult(populations, coverageTracker);
    }

    @GetMapping("/covered")
    @ResponseBody
    public CoveredDTO getCovered(@RequestParam Integer chromosomeSeq) {
        System.out.println("GET /api/java-structure/covered 被调用");
        Map<Chromosome, Integer> chromosomeSequenceMap = getChromosomeSequenceMap();
        //查询value为chromosomeSeq的Integer
        for (Map.Entry<Chromosome, Integer> entry : chromosomeSequenceMap.entrySet()) {
            if (Objects.equals(entry.getValue(), chromosomeSeq)) {
                CoveredDTO coveredDTO = new CoveredDTO();
                Chromosome chromosome = entry.getKey();
                coveredDTO.setCoveredLine(coverageTracker.getLinesCoveredBy(chromosome));
                coveredDTO.setGenes(chromosome.getGenes());
                coveredDTO.setChromosomeId(entry.getValue().toString());
                return coveredDTO;
            }
        }
        return new CoveredDTO();
    }

    private Nature checkAndGetNature(String sessionId) {
        return sessionManager.getNature(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("无效的sessionId"));
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
        return coverageTracker.getChromosomeSequenceMap();
    }
}