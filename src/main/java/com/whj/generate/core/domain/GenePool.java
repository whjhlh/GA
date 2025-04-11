package com.whj.generate.core.domain;


import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author whj
 * @description: 基因池模型
 * @date 2025-04-09 上午12:05
 */
public class GenePool extends BasePool{
    /**
     * 基因库, key为参数索引位，value为参数对应的基因库
     */
    private final Map<Integer, Object[]> parameterGenes = new HashMap<>();
    private final Random random = new Random();
    // 在原有基础上新增以下方法
    private final List<Integer> parameterIndexes = new CopyOnWriteArrayList<>();

    /**
     * 总基因数
     */
    private Integer geneCount = 0;


    public void loadGenes(int paramIndex, Object[] genes) {
        parameterGenes.put(paramIndex, genes.clone());
        geneCount+=genes.length;
        if (!parameterIndexes.contains(paramIndex)) {
            parameterIndexes.add(paramIndex);
            Collections.sort(parameterIndexes); // 保持参数顺序
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

    public Object getRandomGene(int paramIndex) {
        Object[] genes = parameterGenes.get(paramIndex);
        if (genes == null || genes.length == 0) {
            throw new IllegalStateException("No genes loaded for parameter index: " + paramIndex);
        }
        int index = random.nextInt(genes.length);
        return genes[index];
    }

    public Object[] getThresholdValues(int paramIndex) {
        return parameterGenes.getOrDefault(paramIndex, new Object[0]).clone();
    }

    public double getAverageGeneCount(){
        return geneCount/(double)parameterIndexes.size();
    }
}