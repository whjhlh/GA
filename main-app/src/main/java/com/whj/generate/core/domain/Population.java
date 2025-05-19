package com.whj.generate.core.domain;

import com.google.common.collect.Lists;
import com.whj.generate.common.config.PopulationParams;
import com.whj.generate.core.infrastructure.strategy.CombinationStrategy;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Population {
    /**
     * 种群标识1
     */
    private final Class<?> targetClass;
    /**
     * 种群标识2
     */
    private final Method targetMethod;
    /**
     * 种群个体库
     */
    private final Set<Chromosome> chromosomeSet = ConcurrentHashMap.newKeySet();
    /*
     * 种群基因库
     */
    private final GenePool genePool;
    /**
     * 染色体组合分发策略
     */
    private final CombinationStrategy strategy;

    // 缓存优化
    private transient double[] cumulativeFitness;
    private transient double cachedTotalFitness;

    /**
     * 当前覆盖率
     *
     * @param targetClass
     * @param targetMethod
     * @param genePool
     */
    private long currentCoverage;

    // 新增参数快照
    private PopulationParams params;

    public Population(Class<?> targetClass, Method targetMethod, GenePool genePool) {
        this.targetClass = targetClass;
        this.targetMethod = targetMethod;
        this.genePool = genePool;
        this.strategy = new CombinationStrategy(genePool);
        // 创建时自动记录参数快照
        this.params = PopulationParams.snapshot();
    }


    public Class<?> getTargetClass() {
        return targetClass;
    }

    public Method getTargetMethod() {
        return targetMethod;
    }

    public GenePool getGenePool() {
        return genePool;
    }


    public Set<Chromosome> getChromosomeSet() {
        return Collections.unmodifiableSet(chromosomeSet);
    }


    public long getCurrentCoverage() {
        return currentCoverage;
    }

    public void setCurrentCoverage(long currentCoverage) {
        this.currentCoverage = currentCoverage;
    }


    /**
     * 如果不实用synchronized，则会出现线程不安全的情况
     *
     * @param chromosome
     */
    public void addChromosome(Chromosome chromosome) {
        if (!chromosome.getMethod().equals(targetMethod)) {
            throw new IllegalArgumentException(String.format("种群 %s 试图加入种群%s", chromosome.getMethod().getName(), targetMethod.getName()));
        }
        chromosomeSet.add(chromosome);
    }

    /**
     * 批量加入染色体
     *
     * @return
     */
    public void addChromosomeSet(List<Chromosome> chromosomes) {
        chromosomeSet.addAll(chromosomes);
    }
    /**
     * 随机生成某个种群的染色体
     *
     * @return
     */
    public Chromosome initChromosome() {
        Method method = this.getTargetMethod();
        Object[] chromosomeGenes = strategy.generateMinimalCombination();
        return new Chromosome(targetClass, method, chromosomeGenes);
    }

    /**
     * 染色体适应度和覆盖率更新
     */
    public void updateFitnessCache() {
        int size = chromosomeSet.size();
        this.cumulativeFitness = new double[size];
        this.cachedTotalFitness = 0;

        for (int i = 0; i < size; i++) {
            this.cachedTotalFitness += Lists.newArrayList(chromosomeSet).get(i).getFitness() + 1;
            this.cumulativeFitness[i] = this.cachedTotalFitness;
        }
    }

    // 获取方法
    public double[] getCumulativeFitness() {
        return cumulativeFitness;
    }

    public double getCachedTotalFitness() {
        return cachedTotalFitness;
    }
}