package com.whj.generate.core.domain;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;

public class Population {
    private final Class<?> targetClass;
    private final Method targetMethod;
    private final Set<Chromosome> chromosomes = new HashSet<>();
    private final GenePool genePool;

    public Population(Class<?> targetClass, Method targetMethod, GenePool genePool) {
        this.targetClass = targetClass;
        this.targetMethod = targetMethod;
        this.genePool = genePool;
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


    public void addChromosome(Chromosome chromosome) {
        if (!chromosome.getMethod().equals(targetMethod)) {
            throw new IllegalArgumentException("Incompatible chromosome");
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
        //  并行填充基因（根据参数数量自动选择并行度）
        Parameter[] parameters = method.getParameters();
        Object[] chromosomeGenes = new Object[parameters.length];
        IntStream.range(0, parameters.length)
                .parallel()
                .forEach(i -> {
                    chromosomeGenes[i] = this.getGenePool().getRandomGene(i);
                });

        return new Chromosome(method, chromosomeGenes);
    }
}