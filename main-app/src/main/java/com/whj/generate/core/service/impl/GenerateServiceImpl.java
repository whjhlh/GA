package com.whj.generate.core.service.impl;

import com.whj.generate.core.domain.Chromosome;
import com.whj.generate.core.domain.GenePool;
import com.whj.generate.core.domain.Population;
import com.whj.generate.core.infrastructure.strategy.CombinationStrategy;
import com.whj.generate.core.infrastructure.strategy.StrategyFactory;
import com.whj.generate.core.service.GenPoolService;
import com.whj.generate.core.service.GenerateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * 优化点：
 * 1. 线程安全增强
 * 2. 异常处理优化
 * 3. 性能监控支持
 * 4. 代码可读性提升
 * 5. 资源管理改进
 */
@Service
public class GenerateServiceImpl implements GenerateService {
    private static final Logger logger = LoggerFactory.getLogger(GenerateServiceImpl.class);
    private static final int MAX_RETRIES = 5;

    private final GenPoolService genPoolService;
    private final StrategyFactory strategyFactory;
    private final ForkJoinPool geneticThreadPool;

    @Autowired
    public GenerateServiceImpl(GenPoolService genPoolService,
                               StrategyFactory strategyFactory,
                               @Qualifier("geneticForkJoinPool") ForkJoinPool geneticThreadPool) {
        this.genPoolService = genPoolService;
        this.strategyFactory = strategyFactory;
        this.geneticThreadPool = geneticThreadPool;
    }


    @Override
    public Population genertatePopulation(Class<?> clazz, Method method) {
        // 参数校验前置
        validateInput(clazz, method);

        GenePool genePool = genPoolService.initGenePool(clazz, method);
        CombinationStrategy strategy = strategyFactory.createStrategy(genePool);
        Population population = new Population(clazz, method, genePool);

        int populationSize = calculatePopulationSize(genePool);

        generateChromosomesParallel(population, strategy, populationSize);
        ensurePopulationSize(population, strategy, populationSize);

        logger.info("Generated population with {} chromosomes", population.getChromosomeSet().size());
        return population;
    }

    // 并行生成染色体
    private void generateChromosomesParallel(Population population,
                                             CombinationStrategy strategy,
                                             int populationSize) {
        try {
            geneticThreadPool.submit(() ->
                    IntStream.range(0, populationSize)
                            .parallel()
                            .forEach(i -> addChromosomeSafely(population, strategy, population.getTargetClass(), population.getMethod()))
            ).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Population generation interrupted", e);
            throw new PopulationGenerationException("Generation process interrupted", e);
        } catch (ExecutionException e) {
            logger.error("Failed to generate population", e.getCause());
            throw new PopulationGenerationException("Generation execution failed", e.getCause());
        }
    }

    // 线程安全的染色体添加方法
    private void addChromosomeSafely(Population population,
                                     CombinationStrategy strategy,
                                     Class<?> clazz,
                                     Method method) {
        try {
            Object[] genes = strategy.generateCombination();
            Chromosome chromosome = new Chromosome(clazz, method, genes);
            population.addChromosome(chromosome);
        } catch (Exception e) {
            logger.warn("Failed to generate chromosome: {}", e.getMessage());
        }
    }

    // 确保种群规模的兜底逻辑
    private void ensurePopulationSize(Population population,
                                      CombinationStrategy strategy,
                                      int targetSize) {
        AtomicInteger retryCount = new AtomicInteger(0);

        while (population.getChromosomeSet().size() < targetSize && retryCount.getAndIncrement() < MAX_RETRIES) {
            Object[] genes = strategy.generateCombination();
            Chromosome chromosome = new Chromosome(population.getTargetClass(), population.getMethod(), genes);

            if (population.addChromosome(chromosome)) {
                logger.debug("Added backup chromosome");
            }

            if (retryCount.get() % 10 == 0) {
                logger.warn("Population size {} < {}, retry count {}",
                        population.getChromosomeSet().size(), targetSize, retryCount.get());
            }
        }

        if (population.getChromosomeSet().size() < targetSize) {
            logger.error("Failed to reach target population size after {} retries", MAX_RETRIES);
            throw new PopulationGenerationException("Failed to generate sufficient chromosomes");
        }
    }

    // 种群规模计算
    private int calculatePopulationSize(GenePool genePool) {
        int paramCount = genePool.getParameterCount();
        double averageGenes = genePool.getAverageGeneCount();

        if (paramCount <= 0 || averageGenes <= 0) {
            logger.error("Invalid gene pool parameters: paramCount={}, avgGenes={}",
                    paramCount, averageGenes);
            throw new IllegalArgumentException("Invalid gene pool parameters");
        }

        int size = (int) Math.pow(averageGenes, 0.5 * paramCount);
        return Math.max(1, Math.min(size, 10000));  // 限制合理范围
    }

    private void validateInput(Class<?> clazz, Method method) {
        if (clazz == null || method == null) {
            logger.error("Invalid input parameters: class={}, method={}", clazz, method);
            throw new IllegalArgumentException("Class and method must not be null");
        }
    }

    // 自定义异常类
    public static class PopulationGenerationException extends RuntimeException {
        public PopulationGenerationException(String message) {
            super(message);
        }

        public PopulationGenerationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}