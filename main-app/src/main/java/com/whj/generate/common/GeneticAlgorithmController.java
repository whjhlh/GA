package com.whj.generate.common;


import com.whj.generate.biz.Infrastructure.cache.ChromosomeCoverageTracker;
import com.whj.generate.common.config.GeneticAlgorithmConfig;
import com.whj.generate.core.domain.Nature;
import com.whj.generate.core.domain.Population;
import com.whj.generate.core.service.GeneticAlgorithmService;
import com.whj.generate.whjtest.TestForCover;
import org.springframework.stereotype.Controller;

import static com.whj.generate.utill.FileUtil.reportedInFile;

/**
 * @author whj
 * @date 2025-04-10 上午2:46
 */
@Controller
public class GeneticAlgorithmController {
    private final GeneticAlgorithmService geneticAlgorithmService;

    // 构造函数注入
    public GeneticAlgorithmController(GeneticAlgorithmService geneticAlgorithmService) {
        this.geneticAlgorithmService = geneticAlgorithmService;
    }

    /**
     * 程序入口
     */
    public void runGeneticAlgorithm() {
        final Nature nature = new Nature();
        final Class<TestForCover> targetClass = TestForCover.class;
        final String testPhaseName = "test";

        // 初始化环境
        initializeEnvironment(nature, targetClass, testPhaseName);

        // 执行进化过程
        performEvolution(nature);
    }

    private Population initializeEnvironment(Nature nature, Class<?> targetClass, String phaseName) {
        long startTime = System.nanoTime();
        Population population = geneticAlgorithmService.initEnvironment(nature, targetClass, phaseName);
        logOperationDuration(startTime, population, GeneticAlgorithmConfig.INIT_PHASE);
        return population;
    }

    /**
     * 执行进化过程
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
        reportedInFile(duration, population, phase,coverageTracker);
    }
}