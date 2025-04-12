package com.whj.generate.core.domain;

import com.alibaba.fastjson.annotation.JSONField;
import com.whj.coverage.agent.asm.BranchCounter;
import org.objectweb.asm.Type;

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

    @JSONField(serialize = false)
    private final Method method;

    private final Object[] genes;

    // 适应度需要动态计算，不设默认值
    private double fitness;

    public Chromosome(Method method, Object[] genes) {
        Objects.requireNonNull(method, "Method cannot be null");
        this.method = method;
        this.genes = Arrays.copyOf(genes, genes.length); // 防御性拷贝
    }


    public Chromosome(Method method) {
        this.method = method;
        genes = new Object[method.getParameterCount()];
    }

    public double getFitness() {
        return fitness;
    }

    public void setFitness(Double fitness) {
        this.fitness = fitness;
    }

    public Object[] getGenes() {
        return Arrays.copyOf(genes, genes.length);
    }

    public Method getMethod() {
        return method;
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

    public void evaluateFitness() {
        // 生成方法唯一标识
        String className = method.getDeclaringClass().getName().replace('.', '/');
        String methodName = method.getName();
        String methodDesc = Type.getMethodDescriptor(method);
        String methodSignature = className + "." + methodName + methodDesc;

        // 重置覆盖数据
        BranchCounter.reset(methodSignature);

        try {
            // 假设是静态方法，无需实例
            method.invoke(null, genes);
        } catch (Exception e) {
            this.fitness = 0.0; // 执行失败则适应度为0
            return;
        }

        // 计算适应度
        this.fitness = BranchCounter.calculateFitness(methodSignature);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Chromosome that = (Chromosome) object;
        return Objects.deepEquals(genes, that.genes);
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
