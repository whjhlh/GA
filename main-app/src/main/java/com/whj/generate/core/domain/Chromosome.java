package com.whj.generate.core.domain;

import com.alibaba.fastjson.annotation.JSONField;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.IMethodCoverage;
import org.jacoco.core.data.ExecutionDataReader;
import org.jacoco.core.data.ExecutionDataStore;

import java.io.*;
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
    private double fitness;

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

    public void setFitness(Double fitness) {
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

    public void evaluateFitness() {
        String className = method.getDeclaringClass().getName().replace('.', '/');
        String targetMethodName = "evaluateFitness";
        String targetMethodDesc = "()V";

        try {
            method.invoke(null, genes);
        } catch (Exception e) {
            this.fitness = 0.0;
            return;
        }

        File execFile = new File("jacoco.exec");
        if (!execFile.exists()) {
            this.fitness = 0.0;
            return;
        }

        try (InputStream is = new FileInputStream(execFile)) {
            // 1. 创建 ExecutionDataReader
            ExecutionDataReader reader = new ExecutionDataReader(is);

            // 2. 创建 ExecutionDataStore 存储数据
            ExecutionDataStore store = new ExecutionDataStore();

            // 3. 设置数据接收器
            reader.setExecutionDataVisitor(store);

            // 4. 读取数据（需处理可能的异常）
            reader.read();

            // 5. 分析覆盖率
            CoverageBuilder coverageBuilder = new CoverageBuilder();
            Analyzer analyzer = new Analyzer(store, coverageBuilder);
            analyzer.analyzeClass(
                    getClass().getClassLoader().getResourceAsStream(className + ".class"),
                    className
            );

            // 6. 提取目标方法覆盖率
            for (IClassCoverage cc : coverageBuilder.getClasses()) {
                if (cc.getName().equals(className)) {
                    for (IMethodCoverage mc : cc.getMethods()) {
                        if (mc.getName().equals(targetMethodName) && mc.getDesc().equals(targetMethodDesc)) {
                            int covered = mc.getLineCounter().getCoveredCount();
                            int total = mc.getLineCounter().getTotalCount();
                            this.fitness = total == 0 ? 0.0 : (double) covered / total;
                            return;
                        }
                    }
                }
            }
            this.fitness = 0.0;
        } catch (IOException e) {
            e.printStackTrace();
            this.fitness = 0.0;
        }
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
