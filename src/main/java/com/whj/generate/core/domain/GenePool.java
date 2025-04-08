package com.whj.generate.core.domain;


import java.util.HashMap;
import java.util.Map;
import java.util.Random;
/**
 * @description: 基因池模型
 * @author whj
 * @date 2025-04-09 上午12:05
 */
public class GenePool {
    private final Map<Integer, Object[]> parameterGenes = new HashMap<>();
    private final Random random = new Random();

    public void loadGenes(int paramIndex, Object[] genes) {
        parameterGenes.put(paramIndex, genes.clone());
    }

    public Object getRandomGene(int paramIndex) {
        Object[] genes = parameterGenes.get(paramIndex);
        if (genes == null || genes.length == 0) {
            throw new IllegalStateException("No genes loaded for parameter index: " + paramIndex);
        }
        return genes[random.nextInt(genes.length)];
    }

    public Object[] getThresholdValues(int paramIndex) {
        return parameterGenes.getOrDefault(paramIndex, new Object[0]).clone();
    }
}