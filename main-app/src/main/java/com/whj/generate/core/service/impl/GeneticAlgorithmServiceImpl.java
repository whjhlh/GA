package com.whj.generate.core.service.impl;

import com.google.common.collect.Lists;
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
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
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
    ExecutorService processPool = Executors.newWorkStealingPool();

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

        //计算种群适应度
        populationDataHandle(nature, newPopulation);
        return newPopulation;
    }

    @Override
    public ChromosomeCoverageTracker getCoverageTracker() {
        return coverageAnalyzer.getCoverageTracker();
    }

    /**
     * 处理种群数据
     *
     * @param nature
     * @param newPopulation
     */
    private void populationDataHandle(Nature nature, Population newPopulation) {
        coverageAnalyzer.calculatePopulationCoverage(nature, newPopulation);


        // 构建染色体序列
        ChromosomeCoverageTracker coverageTracker = coverageAnalyzer.getCoverageTracker();
        Set<Chromosome> chromosomeSet = newPopulation.getChromosomeSet();
        coverageTracker.buildChromosomeSequenceMap(chromosomeSet);
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
    // 进化过程优化版本
    private void evolveNewGeneration(Nature nature, Population srcPopulation, Population destPopulation) {
        final int targetSize = srcPopulation.getChromosomeSet().size();
        final GenePool genePool = srcPopulation.getGenePool();

        // 使用线程安全集合存储新染色体（修复点1）
        final Set<Chromosome> destSet = ConcurrentHashMap.newKeySet();
        destSet.addAll(destPopulation.getChromosomeSet()); // 保留已有优秀个体

        // 修正循环条件（修复点2）
        while (destSet.size() < targetSize) {
            int batchSize = targetSize - destSet.size();

            // 预生成随机参数（修复点3）
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
                    .toList(); // 使用可变集合（修复点4）

            for(Chromosome child:childList){
                long percent = JaCoCoCoverageAnalyzer.calculateChromosomePercentage(nature, child);
                child.setCoveragePercent(percent);
                destSet.add(child);
            }
        }

        // 最终更新种群（修复点6）
        destPopulation.addChromosomeSet(Lists.newArrayList(destSet));
    }

    // 优化后的子代生成方法
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
        return new Chromosome[] {
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
     * 动态变异率
     *
     * @param pool
     * @return
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
     * @param genes
     * @param pool
     */
    private void mutateGene(Object[] genes, GenePool pool) {
        int mutatePos = ThreadLocalRandom.current().nextInt(genes.length);
        Object[] availableGenes = pool.getParameterGenes().get(mutatePos);
        genes[mutatePos] = availableGenes[ThreadLocalRandom.current().nextInt(availableGenes.length)];
    }


    /**
     * 轮盘赌选择父代
     * <p>
     * 该方法用于在遗传算法中通过轮盘赌方式选择父代染色体轮盘赌选择是基于染色体的适应度进行比例选择的一种方法，
     * 适应度较高的染色体有更高的概率被选中作为父代以产生下一代
     *
     * @param population 种群对象，包含一组染色体
     * @return 返回被选中的父代染色体
     */
    private Chromosome selectParentByRoulette(Population population) {
        final double[] cumulativeFitness = population.getCumulativeFitness();
        final double totalFitness = population.getCachedTotalFitness();

        // 处理极端情况
        ArrayList<Chromosome> chromosomeList = Lists.newArrayList(population.getChromosomeSet());
        if (totalFitness <= 0) {
            return chromosomeList.get(
                    ThreadLocalRandom.current().nextInt(chromosomeList.size())
            );
        }

        // 二分查找
        int index = findIndex(totalFitness, cumulativeFitness);
        return chromosomeList.get(index);
    }

    /**
     * 二分查找
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
