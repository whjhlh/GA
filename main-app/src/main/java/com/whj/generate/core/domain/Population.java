package com.whj.generate.core.domain;

import com.whj.generate.core.infrastructure.strategy.CombinationStrategy;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Population {
    private final Class<?> targetClass;
    private final Method targetMethod;
    private final Set<Chromosome> chromosomes = ConcurrentHashMap.newKeySet();
    private final GenePool genePool;
    private final CombinationStrategy strategy;

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

    private Chromosome selectParent() {
        // 实现轮盘赌选择算法
        return chromosomes.stream() // 简化的随机选择
                .skip((int) (Math.random() * chromosomes.size()))
                .findFirst()
                .orElseThrow();
    }

    /**
     * 如果不实用synchronized，则会出现线程不安全的情况
     * @param chromosome
     */
    public  void addChromosome(Chromosome chromosome) {
        if (!chromosome.getMethod().equals(targetMethod)) {
            throw new IllegalArgumentException(String.format("种群 %s 试图加入种群%s", chromosome.getMethod().getName(), targetMethod.getName()));
        }
        chromosomes.add(chromosome);
    }

    public Set<Chromosome> getChromosomes() {
        return Collections.unmodifiableSet(chromosomes);
    }

    /**
     * 随机生成某个种群的染色体
     * @return
     */
    public Chromosome initChromosome() {
        Method method = this.getTargetMethod();
        Object[] chromosomeGenes = strategy.generateMinimalCombination();
        return new Chromosome(targetClass,method, chromosomeGenes);
    }
}