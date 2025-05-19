package com.whj.generate.core.domain;

import com.google.common.collect.Lists;
import com.whj.generate.core.infrastructure.strategy.CombinationStrategy;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Population extends PopulationBase {
    /**
     * 种群个体库
     */
    private final Set<Chromosome> chromosomeSet = ConcurrentHashMap.newKeySet();
    /*
     * 种群基因库
     */
    private final GenePool genePool;

    // 缓存优化
    private transient double[] cumulativeFitness;
    private transient double cachedTotalFitness;

    /**
     * 当前覆盖率
     */
    private long currentCoverage;


    public Population(Class<?> targetClass, Method targetMethod, GenePool genePool) {
        super(targetClass,  targetMethod);
        this.genePool = genePool;
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
    public boolean addChromosome(Chromosome chromosome) {
        if (!chromosome.getMethod().equals(super.getMethod())) {
            return false;
        }
        chromosomeSet.add(chromosome);
        return true;
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
        Method method = super.getMethod();
        Object[] chromosomeGenes = new Object[1];
        return new Chromosome(super.getTargetClass(), method, chromosomeGenes);
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