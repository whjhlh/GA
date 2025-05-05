package com.whj.generate.core.service.impl;

import com.whj.generate.biz.Infrastructure.JaCoCoCoverageAnalyzer;
import com.whj.generate.biz.Infrastructure.cache.ChromosomeCoverageTracker;
import com.whj.generate.common.config.GeneticAlgorithmConfig;
import com.whj.generate.core.domain.Chromosome;
import com.whj.generate.core.domain.GenePool;
import com.whj.generate.core.domain.Nature;
import com.whj.generate.core.domain.Population;
import com.whj.generate.core.infrastructure.PoolLoader;
import com.whj.generate.core.infrastructure.strategy.SelectionStrategy;
import com.whj.generate.core.service.GeneticAlgorithmService;
import com.whj.generate.utill.ReflectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;


/**
 * @author whj
 * @date 2025-02-22 下午5:40
 */
@Service
public class GeneticAlgorithmServiceImpl implements GeneticAlgorithmService {
    private static final Logger logger = LoggerFactory.getLogger(GeneticAlgorithmServiceImpl.class);

    private final PoolLoader<GenePool> poolLoader;
    private final ForkJoinPool geneticThreadPool;
    @Autowired
    private final JaCoCoCoverageAnalyzer coverageAnalyzer;
    @Autowired
    @Qualifier("eliteDiverseStrategy")
    private SelectionStrategy selectionStrategy;


    @Autowired
    public GeneticAlgorithmServiceImpl(
            PoolLoader<GenePool> poolLoader,
            @Qualifier("geneticForkJoinPool") ForkJoinPool geneticThreadPool,
            JaCoCoCoverageAnalyzer coverageAnalyzer
    ) {
        this.poolLoader = poolLoader;
        this.geneticThreadPool = geneticThreadPool;
        this.coverageAnalyzer = coverageAnalyzer;
    }


    /**
     * 初始化种群
     *
     * @param clazz 方法
     */
    @Override
    public Population initEnvironment(Nature nature, Class<?> clazz, String methodName) {
        Method testMethod = ReflectionUtil.findMethod(clazz, methodName);
        Population population = createPopulationModel(clazz, testMethod);
        // 处理初始化种群
        processInitPopulation(population);
        // 处理初始化种群数据
        populationDataHandle(nature, population);
        return population;
    }

    /**
     * 进化种群
     *
     * @param nature 种群
     * @param count
     * @return
     */
    @Override
    public Population evolvePopulation(Nature nature, Integer count) {
        Population population = nature.getPopulationList().get(count);
        Population newPopulation = createNewPopulation(population);

        // 精英保留（保留历史最优解）
        preserveElites(population, newPopulation);

        // 进化生成新个体
        evolveNewGeneration(nature, population, newPopulation);

        populationDataHandle(nature, newPopulation);
        return newPopulation;
    }
    @Override
    public ChromosomeCoverageTracker getCoverageTracker() {
        return coverageAnalyzer.getCoverageTracker();
    }
    /**
     * 处理种群数据
     * @param nature
     * @param newPopulation
     */
    private void populationDataHandle(Nature nature, Population newPopulation) {
        coverageAnalyzer.calculatePopulationCoverage(nature, newPopulation);
        nature.addPopulation(newPopulation);

        Set<Chromosome> chromosomes = newPopulation.getChromosomeSet();
        // 构建染色体序列
        coverageAnalyzer.getCoverageTracker().buildChromosomeSequenceMap(chromosomes);
    }


    /**
     * 初始化种群大小 = (avg_genes)^(k/2)
     *
     * @param population
     * @return
     */
    private int calculatePopulationSize(Population population) {
        if (null == population) {
            return 0;
        }
        GenePool genePool = population.getGenePool();
        return (int) Math.pow(genePool.getAverageGeneCount(), 0.5 * genePool.getParameterCount());
    }

    private Population createPopulationModel(Class<?> clazz, Method method) {
        GenePool genePool = poolLoader.initializePool(clazz, method);
        return new Population(clazz, method, genePool);
    }

    private Population createNewPopulation(Population old) {
        return new Population(
                old.getTargetClass(),
                old.getTargetMethod(),
                old.getGenePool() // 关键点：继承基因库
        );
    }

    /**
     * 处理初始化种群
     *
     * @param population
     */
    private void processInitPopulation(Population population) {
        if (null == population) {
            return;
        }
        int populationSize = calculatePopulationSize(population);
        try {
            geneticThreadPool.submit(() -> IntStream.range(0, populationSize)
                    .parallel()
                    .forEach(i -> {
                        Chromosome chromosome = population.initChromosome();
                        population.addChromosome(chromosome);
                    })
            ).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        //兜底补充，解决并行带来的问题
        while (population.getChromosomeSet().size() < populationSize) {
            Chromosome chromosome = population.initChromosome();
            population.addChromosome(chromosome);
        }
    }

    /**
     * 精英保留（保留历史最优解）
     *
     * @param src
     * @param dest
     */
    private void preserveElites(Population src, Population dest) {
        List<Chromosome> select = selectionStrategy.select(src, coverageAnalyzer.getCoverageTracker());
        dest.addChromosomeSet(select);
    }

    /**
     * 进化下一代
     *
     * @param nature
     * @param srcPopulation
     * @param destPopulation
     */
    private void evolveNewGeneration(Nature nature, Population srcPopulation, Population destPopulation) {
        GenePool genePool = srcPopulation.getGenePool();
        while (destPopulation.getChromosomeSet().size() < srcPopulation.getChromosomeSet().size()) {
            Chromosome child = generateChild(srcPopulation, genePool);
            long percent = JaCoCoCoverageAnalyzer.calculateChromosomePercentage(nature, child);
            child.setCoveragePercent(percent);
            destPopulation.addChromosome(child);

        }
    }

    private Chromosome generateChild(Population population, GenePool genePool) {
        // 轮盘赌选择父代
        Chromosome parent1 = selectParentByRoulette(population);
        Chromosome parent2 = selectParentByRoulette(population);

        // 交叉变异
        return performGeneticOperations(parent1, parent2, genePool);
    }

    private Chromosome performGeneticOperations(Chromosome p1, Chromosome p2, GenePool pool) {
        Object[] genes = Arrays.copyOf(p1.getGenes(), p1.getGenes().length);

        // 单点交叉
        if (Math.random() < GeneticAlgorithmConfig.CROSSOVER_RATE) {
            int crossPoint = ThreadLocalRandom.current().nextInt(genes.length);
            for (int i = crossPoint; i < genes.length; i++) {
                genes[i] = p2.getGenes()[i];
            }
        }

        // 基于基因库的变异
        if (Math.random() < getDynamicMutationRate(pool)) {
            mutateGene(genes, pool);
        }

        return new Chromosome(p1.getTargetClass(), p1.getMethod(), genes);
    }

    /**
     * 基因变异
     *
     * @param genes
     * @param pool
     */
    private void mutateGene(Object[] genes, GenePool pool) {
        int mutatePos = ThreadLocalRandom.current().nextInt(genes.length);
        Object[] availableGenes = pool.getParameterGenes().get(mutatePos);
        genes[mutatePos] = availableGenes[ThreadLocalRandom.current().nextInt(availableGenes.length)];
    }

    /**
     * 动态计算变异率
     *
     * @param pool
     * @return
     */
    private double getDynamicMutationRate(GenePool pool) {
        // 基因类型越多变异率越高
        double diversityFactor = pool.getAverageGeneCount() / 10.0;
        return Math.min(GeneticAlgorithmConfig.MUTATION_RATE * (0.3 + 0.1 * diversityFactor), 0.35);
    }

    /**
     * 轮盘赌选择父代
     *
     * @param population
     * @return
     */
    private Chromosome selectParentByRoulette(Population population) {
        double totalFitness = population.getChromosomeSet().stream()
                .mapToDouble(c -> c.getCoveragePercent() + 1) // 避免0概率
                .sum();

        double threshold = ThreadLocalRandom.current().nextDouble() * totalFitness;
        double accumulator = 0;

        for (Chromosome c : population.getChromosomeSet()) {
            accumulator += (c.getCoveragePercent() + 1);
            if (accumulator >= threshold) {
                return c;
            }
        }
        return population.getChromosomeSet().iterator().next();
    }
}
