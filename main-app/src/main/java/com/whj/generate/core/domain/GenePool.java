package com.whj.generate.core.domain;


import java.io.Serial;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author whj
 * @description: 基因池模型
 * @date 2025-04-09 上午12:05
 */
public class GenePool extends BasePool {
    /**
     * serialVersionUID
     */
    @Serial
    private static final long serialVersionUID = 14387891234232L;
    /**
     * 基因库, key为参数索引位，value为参数对应的基因库
     */
    private final Map<Integer, Object[]> parameterGenes = new HashMap<>();
    /**
     * 参数索引
     */
    private final List<Integer> parameterIndexes = new CopyOnWriteArrayList<>();

    /**
     * 加载指定参数索引的基因列表
     */
    public synchronized void loadGenes(int paramIndex, Object[] genes) {
        if (genes == null || genes.length == 0) {
            throw new IllegalArgumentException("基因数组不能为空");
        }

        parameterGenes.put(paramIndex, genes.clone());

        if (!parameterIndexes.contains(paramIndex)) {
            parameterIndexes.add(paramIndex);
            parameterIndexes.sort(Integer::compareTo);
        }
    }

    /**
     * 获取参数位的基因种类数
     *
     * @return
     */
    public List<Integer> getParameterIndexes() {
        return Collections.unmodifiableList(parameterIndexes);
    }

    public int getParameterCount() {
        return parameterIndexes.size();
    }

    public Map<Integer, Object[]> getParameterGenes() {
        return parameterGenes;
    }

    /**
     * 获取参数位的基因种类数
     */
    public int getGeneTypeCount(int paramIndex) {
        return parameterGenes.getOrDefault(paramIndex, new Object[0]).length;
    }

    public Object[] getThresholdValues(int paramIndex) {
        return parameterGenes.getOrDefault(paramIndex, new Object[0]).clone();
    }

    public double getAverageGeneCount() {
        if (parameterIndexes.isEmpty()) return 0;
        int total = parameterGenes.values().stream().mapToInt(arr -> arr.length).sum();
        return total / (double) parameterIndexes.size();
    }
    /**
     * 获取基因库的最大基因数
     */
    public int getMaxGeneCount() {
        return parameterGenes.values().stream()
                .mapToInt(arr -> arr.length)
                .max()
                .orElse(0);
    }

}