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
 * 生成服务实现类
 */
@Service
public class GenerateServiceImpl implements GenerateService {
    private static final Logger logger = LoggerFactory.getLogger(GenerateServiceImpl.class);
    private static final int MAX_RETRIES = 5;

    private final GenPoolService genPoolService;
    private final StrategyFactory strategyFactory;
    private final ForkJoinPool geneticThreadPool;

    /**
     * 构造函数注入依赖
     *
     * @param genPoolService 基因池服务
     * @param strategyFactory 策略工厂
     * @param geneticThreadPool 并行处理线程池
     */
    @Autowired
    public GenerateServiceImpl(GenPoolService genPoolService,
                               StrategyFactory strategyFactory,
                               @Qualifier("geneticForkJoinPool") ForkJoinPool geneticThreadPool) {
        this.genPoolService = genPoolService;
        this.strategyFactory = strategyFactory;
        this.geneticThreadPool = geneticThreadPool;
    }

    /**
     * 并行生成种群
     *
     * @param clazz 类信息
     * @param method 方法信息
     * @return 生成的种群对象
     */
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

        logger.info("已生成包含 {} 条染色体的种群", population.getChromosomeSet().size());
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
            logger.error("种群生成过程中被中断: {}", e.getMessage());
            throw new PopulationGenerationException("生成过程被中断", e);
        } catch (ExecutionException e) {
            logger.error("生成种群失败: {}", e.getCause().getMessage());
            throw new PopulationGenerationException("生成执行失败", e.getCause());
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
            logger.warn("生成染色体失败: {}", e.getMessage());
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
                logger.debug("成功添加备份染色体");
            }

            if (retryCount.get() % 10 == 0) {
                logger.warn("当前种群大小 {} < {}, 重试次数 {}",
                        population.getChromosomeSet().size(), targetSize, retryCount.get());
            }
        }

        if (population.getChromosomeSet().size() < targetSize) {
            logger.error("经过 {} 次重试后仍未能达到目标种群大小", MAX_RETRIES);
            throw new PopulationGenerationException("未能生成足够的染色体");
        }
    }

    // 种群规模计算
    private int calculatePopulationSize(GenePool genePool) {
        int paramCount = genePool.getParameterCount();
        double averageGenes = genePool.getAverageGeneCount();

        if (paramCount <= 0 || averageGenes <= 0) {
            logger.error("基因池参数无效: paramCount={}, avgGenes={}",
                    paramCount, averageGenes);
            throw new IllegalArgumentException("基因池参数必须有效");
        }

        int size = (int) Math.pow(averageGenes, 0.5 * paramCount);
        return Math.max(1, Math.min(size, 10000));  // 限制合理范围
    }

    // 输入参数校验
    private void validateInput(Class<?> clazz, Method method) {
        if (clazz == null || method == null) {
            logger.error("输入参数无效: class={}, method={}", clazz, method);
            throw new IllegalArgumentException("类和方法不能为空");
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
