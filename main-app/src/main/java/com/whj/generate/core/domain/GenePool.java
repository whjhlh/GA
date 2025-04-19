package com.whj.generate.core.domain;


import java.io.Serial;
import java.util.*;
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
     * 总基因数
     */
    private Integer geneCount = 0;

    private final Random random = new Random();


    public void loadGenes(int paramIndex, Object[] genes) {
        parameterGenes.put(paramIndex, genes.clone());
        geneCount += genes.length;
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

    public double getAverageGeneCount() {
        return geneCount / (double) parameterIndexes.size();
    }

    /**
     * 生成参数组合
     *
     * @param paramIndexes
     * @return
     */
    public Object[][] getParameterCombinations(int[] paramIndexes) {
        List<Object[]> combinations = new ArrayList<>();
        int total = 1;

        // 计算总组合数
        for (int idx : paramIndexes) {
            total *= getGeneTypeCount(idx);
        }

        // 生成笛卡尔积
        int[] counters = new int[paramIndexes.length];
        while (combinations.size() < total) {
            Object[] combo = new Object[paramIndexes.length];
            for (int i = 0; i < paramIndexes.length; i++) {
                combo[i] = parameterGenes.get(paramIndexes[i])[counters[i]];
            }
            combinations.add(combo);

            // 更新计数器
            for (int i = 0; i < counters.length; i++) {
                if (++counters[i] < getGeneTypeCount(paramIndexes[i])) {
                    break;
                }
                counters[i] = 0;
            }
        }
        return combinations.toArray(new Object[0][]);
    }

    // 基因有效性验证
    public boolean validateGene(int paramIndex, Object gene) {
        return Arrays.stream(parameterGenes.get(paramIndex))
                .anyMatch(g -> Objects.equals(g, gene));
    }
}