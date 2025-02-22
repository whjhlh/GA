package com.whj.generate.model;

import java.lang.reflect.Method;

/**
 * @author whj
 * @description: 染色体模型
 * @date 2025-02-22 下午5:41
 */
public class Chromosome {
    /**
     * 参数方法
     */
    Method method;
    /**
     * 基因组
     */
    Object[] genes;
    /**
     * 适应度
     */
    double fitness;

    public Chromosome(Method method) {
        this.method = method;
        genes = new Object[method.getParameterCount()];
        fitness = 0;
    }

    public Object[] getGenes() {
        return genes;
    }

    public void setGenes(Object[] genes) {
        this.genes = genes;
    }

    public double getFitness() {
        return fitness;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }
}
