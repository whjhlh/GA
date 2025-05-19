package com.whj.generate.core.domain;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

/**
 * @author whj
 * @description: 染色体领域模型
 * @date 2025-02-22 下午5:41
 */
public class Chromosome extends ChromosomeBase {
    /**
     * 基因
     */
    private final Object[] genes;

    /**
     * 覆盖率
     */
    private long coveragePercent;
    /**
     * 适应度
     */
    private long fitness;

    /**
     * @param targetClass
     * @param method
     * @param genes
     */

    public Chromosome(Class<?> targetClass, Method method, Object[] genes) {
        super(targetClass, method);
        Objects.requireNonNull(method, "Method cannot be null");
        this.genes = Arrays.copyOf(genes, genes.length); // 防御性拷贝
    }


    public Chromosome(Class<?> targetClass, Method method) {
        super(targetClass, method);
        genes = new Object[method.getParameterCount()];
    }

    public double getCoveragePercent() {
        return coveragePercent;
    }

    public void setCoveragePercent(long coveragePercent) {
        this.coveragePercent = coveragePercent;
    }

    public Object[] getGenes() {
        return Arrays.copyOf(genes, genes.length);
    }

    public double getFitness() {
        return fitness;
    }

    public void setFitness(long fitness) {
        this.fitness = fitness;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Chromosome that = (Chromosome) object;
        return that.hashCode() == this.hashCode();
    }

    /**
     * 重写hashCode方法，保证两个染色体对象相等时，hashCode也相等
     */
    @Override
    public int hashCode() {
        int hash = 1;
        for (Object gene : genes) {
            hash = hash * 31 + (gene == null ? 0 : gene.hashCode());
        }
        return hash;
    }


}
