package com.whj.generate.core.domain;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

/**
 * @author whj
 * @description: 染色体模型
 * @date 2025-02-22 下午5:41
 */
public class Chromosome implements Serializable {
    /**
     * 序列化
     */
    private static final long serialVersionUID = 124238713634L;
    /**
     * 目标测试类
     */
    @JSONField(serialize = false)
    private final Class<?> targetClass;

    /**
     * 目标方法
     */
    @JSONField(serialize = false)
    private final Method method;
    /**
     * 基因
     */
    private final Object[] genes;

    // 适应度需要动态计算，不设默认值
    private long fitness;

    public Chromosome(Class<?> targetClass, Method method, Object[] genes) {
        this.targetClass = targetClass;
        Objects.requireNonNull(method, "Method cannot be null");
        this.method = method;
        this.genes = Arrays.copyOf(genes, genes.length); // 防御性拷贝
    }


    public Chromosome(Class<?> targetClass, Method method) {
        this.targetClass = targetClass;
        this.method = method;
        genes = new Object[method.getParameterCount()];
    }

    public double getFitness() {
        return fitness;
    }

    public void setFitness(long fitness) {
        this.fitness = fitness;
    }

    public Object[] getGenes() {
        return Arrays.copyOf(genes, genes.length);
    }

    public Method getMethod() {
        return method;
    }

    public Class<?> getTargetClass() {
        return targetClass;
    }

    /**
     * 是否执行过，如果适应度为null,则认定为未执行过
     */
    public boolean isExecuted() {
        return fitness != 0;
    }

    /**
     * 判断两个染色体是否属于同一种基因型
     *
     * @param other
     * @return
     */
    public boolean isSameSpecies(Chromosome other) {
        return this.method.equals(other.method);
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
