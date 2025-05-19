package com.whj.generate.core.service.impl;

import com.google.common.collect.Lists;
import com.whj.generate.biz.Infrastructure.cache.ChromosomeCoverageTracker;
import com.whj.generate.common.config.GeneticAlgorithmConfig;
import com.whj.generate.core.domain.Chromosome;
import com.whj.generate.core.domain.GenePool;
import com.whj.generate.core.domain.Nature;
import com.whj.generate.core.domain.Population;
import com.whj.generate.core.infrastructure.strategy.SelectionStrategy;
import com.whj.generate.core.service.CoverageService;
import com.whj.generate.core.service.GenPoolService;
import com.whj.generate.core.service.GeneticAlgorithmService;
import com.whj.generate.core.service.GenerateService;
import com.whj.generate.utill.ReflectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;


/**
 * 遗传算法实现类
 * 负责初始化种群、进化种群等核心逻辑
 */
@Service
public class GeneticAlgorithmServiceImpl implements GeneticAlgorithmService {
    private static final Logger logger = LoggerFactory.getLogger(GeneticAlgorithmServiceImpl.class);

    // 基因加载器
    private final GenPoolService genPoolService;
    // 遗传算法线程池
    private final ForkJoinPool geneticThreadPool;
    // 染色体覆盖率跟踪器
    private final ChromosomeCoverageTracker coverageTracker;
    // 覆盖率服务
    private final CoverageService coverageService;
    // 选择策略
    private final SelectionStrategy selectionStrategy;
    private final GenerateService generateService;

    @Autowired
    public GeneticAlgorithmServiceImpl(
            GenPoolService genPoolService,
            ChromosomeCoverageTracker coverageTracker,
            CoverageService coverageService,
            @Qualifier("geneticForkJoinPool") ForkJoinPool geneticThreadPool,
            @Qualifier("eliteDiverseStrategy") SelectionStrategy selectionStrategy,
            GenerateService generateService) {
        this.genPoolService = genPoolService;
        this.geneticThreadPool = geneticThreadPool;
        this.coverageTracker = coverageTracker;
        this.coverageService = coverageService;
        this.selectionStrategy = selectionStrategy;
        this.generateService = generateService;
    }

    /**
     * 初始化种群
     *
     * @param clazz      方法所属类
     * @param methodName 方法名
     * @return 初始化后的种群
     */
    @Override
    public Population initEnvironment(Nature nature, Class<?> clazz, String methodName) {
        Method testMethod = ReflectionUtil.findMethod(clazz, methodName);
        Population population = generateService.genertatePopulation(clazz, testMethod);
        // 处理初始化种群数据
        populationDataHandle(nature, population);
        return population;
    }

    /**
     * 进化种群
     *
     * @param nature 自然环境
     * @param count  种群计数
     * @return 进化后的种群
     */
    @Override
    public Population evolvePopulation(Nature nature, Integer count) {
        Population population = nature.getPopulationList().get(count);
        Population newPopulation = createNewPopulation(population);

        // 精英保留（保留历史最优解）
        preserveElites(nature, population, newPopulation);

        // 进化生成新个体
        evolveNewGeneration(population, newPopulation);

        //计算种群适应度
        populationDataHandle(nature, newPopulation);
        return newPopulation;
    }

    /**
     * 处理种群数据
     *
     * @param nature        自然环境
     * @param newPopulation 新种群
     */
    private void populationDataHandle(Nature nature, Population newPopulation) {
        coverageService.calculatePopulationCoverage(nature, newPopulation);
        Set<Chromosome> chromosomeSet = newPopulation.getChromosomeSet();
        coverageTracker.buildChromosomeSequenceMap(chromosomeSet);
    }

    /**
     * 初始化种群大小计算
     *
     * @param population 种群
     * @return 种群大小
     */
    private int calculatePopulationSize(Population population) {
        if (null == population) {
            return 0;
        }
        GenePool genePool = population.getGenePool();
        return (int) Math.pow(genePool.getAverageGeneCount(), 0.5 * genePool.getParameterCount());
    }

    /**
     * 创建种群模型（初始化）
     *
     * @param clazz  类
     * @param method 方法
     * @return 种群模型
     */
    private Population createPopulationModel(Class<?> clazz, Method method) {
        GenePool genePool = genPoolService.initGenePool(clazz, method);
        return new Population(clazz, method, genePool);
    }

    /**
     * 创建新种群（进化）
     *
     * @param src 源种群
     * @return 新种群
     */
    private Population createNewPopulation(Population src) {
        return new Population(
                src.getTargetClass(),
                src.getMethod(),
                src.getGenePool() // 关键点：继承基因库
        );
    }

    /**
     * 处理初始化种群
     *
     * @param population 种群
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
     * @param nature 自然环境
     * @param src    源种群
     * @param dest   目标种群
     */
    private void preserveElites(Nature nature, Population src, Population dest) {
        List<Chromosome> select = selectionStrategy.select(nature, src);
        dest.addChromosomeSet(select);
    }

    /**
     * 种群进化
     *
     * @param srcPopulation  源种群
     * @param destPopulation 目标种群
     */
    private void evolveNewGeneration(Population srcPopulation, Population destPopulation) {
        final int targetSize = srcPopulation.getChromosomeSet().size();
        final GenePool genePool = srcPopulation.getGenePool();

        // 使用线程安全集合存储新染色体
        final Set<Chromosome> destSet = ConcurrentHashMap.newKeySet();
        destSet.addAll(destPopulation.getChromosomeSet());

        // 保证生成一定数量的染色体，保证多样性
        while (destSet.size() < targetSize) {
            int batchSize = targetSize - destSet.size();

            // 预生成随机参数
            final double[] crossoverRandoms = ThreadLocalRandom.current().doubles(batchSize).toArray();
            final double[] mutationRandoms = ThreadLocalRandom.current().doubles(batchSize).toArray();

            // 批量生成子代（保持并行处理）
            List<Chromosome> childList = IntStream.range(0, batchSize)
                    .parallel()
                    .mapToObj(i -> generateChild(
                            srcPopulation,
                            genePool,
                            crossoverRandoms[i],
                            mutationRandoms[i]
                    ))
                    .toList();
            destSet.addAll(childList);
        }
        // 最终更新种群
        destPopulation.addChromosomeSet(Lists.newArrayList(destSet));
    }

    /**
     * 子代生成
     *
     * @param population      种群
     * @param genePool        基因库
     * @param crossoverRandom 交叉随机数
     * @param mutationRandom  变异随机数
     * @return 子代染色体
     */
    private Chromosome generateChild(Population population, GenePool genePool,
                                     double crossoverRandom, double mutationRandom) {
        // 批量选择父代（缓存提升）
        Chromosome[] parents = selectParentsBulk(population);
        return performGeneticOperations(
                parents[0],
                parents[1],
                genePool,
                crossoverRandom,
                mutationRandom
        );
    }

    // 批量选择父代（减少缓存访问次数）
    private Chromosome[] selectParentsBulk(Population population) {
        final double[] cumFitness = population.getCumulativeFitness();
        final double totalFitness = population.getCachedTotalFitness();

        // 单次阈值生成选择两个父代
        double threshold1 = ThreadLocalRandom.current().nextDouble() * totalFitness;
        double threshold2 = ThreadLocalRandom.current().nextDouble() * totalFitness;

        int index1 = findIndex(threshold1, cumFitness);
        int index2 = findIndex(threshold2, cumFitness);

        Set<Chromosome> chromosomeSet = population.getChromosomeSet();
        ArrayList<Chromosome> chromosomeLists = Lists.newArrayList(chromosomeSet);
        return new Chromosome[]{
                chromosomeLists.get(index1),
                chromosomeLists.get(index2)
        };
    }

    // 遗传操作
    private Chromosome performGeneticOperations(Chromosome p1, Chromosome p2,
                                                GenePool pool, double crossoverRandom,
                                                double mutationRandom) {
        Object[] genes = Arrays.copyOf(p1.getGenes(), p1.getGenes().length);

        // 合并交叉与变异决策
        boolean doCrossover = crossoverRandom < GeneticAlgorithmConfig.CROSSOVER_RATE;
        boolean doMutation = mutationRandom < getDynamicMutationRate(pool);

        // 单点交叉优化
        if (doCrossover) {
            int crossPoint = ThreadLocalRandom.current().nextInt(genes.length);
            System.arraycopy(p2.getGenes(), crossPoint, genes, crossPoint, genes.length - crossPoint);
        }

        // 变异操作
        if (doMutation) {
            mutateGene(genes, pool);
        }

        return new Chromosome(p1.getTargetClass(), p1.getMethod(), genes);
    }

    /**
     * 动态变异率计算
     *
     * @param pool 基因库
     * @return 动态变异率
     */
    private double getDynamicMutationRate(GenePool pool) {
        // 基因类型越多变异率越高
        double diversityFactor = pool.getAverageGeneCount() / pool.getMaxGeneCount();
        if (GeneticAlgorithmConfig.MUTATION_RATE == -1) {
            return diversityFactor;
        }
        return (GeneticAlgorithmConfig.MUTATION_RATE + diversityFactor) / 2;
    }

    /**
     * 基因变异
     *
     * @param genes 基因数组
     * @param pool  基因库
     */
    private void mutateGene(Object[] genes, GenePool pool) {
        int mutatePos = ThreadLocalRandom.current().nextInt(genes.length);
        Object[] availableGenes = pool.getParameterGenes().get(mutatePos);
        genes[mutatePos] = availableGenes[ThreadLocalRandom.current().nextInt(availableGenes.length)];
    }

    /**
     * 二分查找
     *
     * @param totalFitness
     * @param cumulativeFitness
     * @return
     */
    private static int findIndex(double totalFitness, double[] cumulativeFitness) {
        final double threshold = ThreadLocalRandom.current().nextDouble() * totalFitness;
        int index = Arrays.binarySearch(cumulativeFitness, threshold);
        index = (index >= 0) ? index : Math.min(-index - 1, cumulativeFitness.length - 1);
        return index;
    }

}
