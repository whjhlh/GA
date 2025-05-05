package com.whj.generate.common;


import com.whj.generate.biz.Infrastructure.cache.ChromosomeCoverageTracker;
import com.whj.generate.common.config.GeneticAlgorithmConfig;
import com.whj.generate.common.req.AlgorithmRequest;
import com.whj.generate.core.domain.Nature;
import com.whj.generate.core.domain.Population;
import com.whj.generate.core.service.GeneticAlgorithmService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import static com.whj.generate.utill.FileUtil.reportedInFile;

/**
 * @author whj
 * @date 2025-04-10 上午2:46
 */
@Controller

@RequestMapping("/genetic-algorithm")
public class GeneticAlgorithmController {
    private final GeneticAlgorithmService geneticAlgorithmService;

    // 构造函数注入
    public GeneticAlgorithmController(GeneticAlgorithmService geneticAlgorithmService) {
        this.geneticAlgorithmService = geneticAlgorithmService;
    }

    // 新增的请求参数封装类


    /**
     * 新增的接口：接收类名和方法名
     *
     * @param request 包含className和methodName的请求体
     */
    @PostMapping("/start")
    @ResponseBody
    public String startAlgorithm(@RequestBody AlgorithmRequest request) {
        try {
            // 通过反射获取目标类
            Class<?> targetClass = Class.forName("com.whj.generate.whjtest." + request.getClassName());

            // 执行进化算法
            runGeneticAlgorithm(targetClass, request.getMethodName());
            return "进化算法执行成功 "
                    + request.getClassName() + "#" + request.getMethodName();
        } catch (ClassNotFoundException e) {
            return "未找到该类: " + request.getClassName();
        } catch (Exception e) {
            return "进化算法执行失败: " + e.getMessage();
        }
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
}