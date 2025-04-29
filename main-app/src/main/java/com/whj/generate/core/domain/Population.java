package com.whj.generate.core.domain;

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

    /**
     * 当前覆盖率
     *
     * @param targetClass
     * @param targetMethod
     * @param genePool
     */
    private long currentCoverage;


    public Population(Class<?> targetClass, Method targetMethod, GenePool genePool) {
        this.targetClass = targetClass;
        this.targetMethod = targetMethod;
        this.genePool = genePool;
        this.strategy = new CombinationStrategy(genePool);
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
     * @return
     */
    public void addChromosomeSet(List<Chromosome> chromosomes) {
        chromosomeSet.addAll(chromosomes);
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
     * 随机生成某个种群的染色体
     *
     * @return
     */
    public Chromosome initChromosome() {
        Method method = this.getTargetMethod();
        Object[] chromosomeGenes = strategy.generateMinimalCombination();
        return new Chromosome(targetClass, method, chromosomeGenes);
    }

    public double getAverageFitness() {
        if (chromosomeSet.isEmpty()) {
            return 0;
        }
        return chromosomeSet.stream()
                .mapToDouble(Chromosome::getCoveragePercent)
                .average()
                .orElse(0);
    }

    /**
     * 获取当前种群的最大适应度
     *
     * @return
     */

    public double getMaxFitness() {
        if (chromosomeSet.isEmpty()) {
            return 0;
        }
        return chromosomeSet.stream()
                .mapToDouble(Chromosome::getCoveragePercent)
                .max()
                .orElse(0);
    }

    /**
     * 计算与当前幸存者的基因差异度
     *
     * @param c
     * @return
     */
    public double calculateDiversityScore(Chromosome c) {
        // 计算与当前幸存者的基因差异度
        return this.getChromosomeSet().stream()
                .mapToDouble(other -> calculateGeneDifference(c, other))
                .average()
                .orElse(0);
    }

    /**
     * 计算两个染色体的基因差异度
     *
     * @param a
     * @param b
     * @return
     */
    private double calculateGeneDifference(Chromosome a, Chromosome b) {
        Object[] genesA = a.getGenes();
        Object[] genesB = b.getGenes();

        int diffCount = 0;
        for (int i = 0; i < genesA.length; i++) {
            if (!genesA[i].equals(genesB[i])) {
                diffCount++;
            }
        }
        return (double) diffCount / genesA.length;
    }

    /**
     * 染色体交叉
     * @param p1
     * @param p2
     * @return
     */
    private Chromosome enhancedCrossover(Chromosome p1, Chromosome p2) {
        Object[] genes = new Object[p1.getGenes().length];
        GenePool pool = getGenePool();

        for (int i = 0; i < genes.length; i++) {
            // 50%概率选择优势基因
            if (pool.getGeneTypeCount(i) > 5) {
                genes[i] = selectDominantGene(p1.getGenes()[i], p2.getGenes()[i]);
            } else {
                genes[i] = Math.random() < 0.5 ? p1.getGenes()[i] : p2.getGenes()[i];
            }
        }
        return new Chromosome(p1.getTargetClass(), p1.getMethod(), genes);
    }
    private Object selectDominantGene(Object g1, Object g2) {
        // 实现基于类型特征的基因选择策略
        if (g1 instanceof Number && g2 instanceof Number) {
            return ((Number) g1).doubleValue() > ((Number) g2).doubleValue() ? g1 : g2;
        }
        return Math.random() < 0.5 ? g1 : g2;
    }
}