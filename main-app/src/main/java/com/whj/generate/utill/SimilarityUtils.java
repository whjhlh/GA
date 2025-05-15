package com.whj.generate.utill;



import com.google.common.collect.Sets;

import java.util.List;
import java.util.Set;

/**
 * 相似度工具类，提供 Jaccard 相似度计算及种群平均相似度方法
 * @author whj
 * @date 2025-05-15 下午5:23
 */
public class SimilarityUtils {

    /**
     * 计算两个集合的 Jaccard 相似度
     *
     * @param a 第一集合
     * @param b 第二集合
     * @return Jaccard 相似度 (0.0 ~ 1.0)
     */
    public static double jaccardSimilarity(Set<Integer> a, Set<Integer> b) {
        if ((a == null || a.isEmpty()) && (b == null || b.isEmpty())) {
            return 0.0;
        }
        int intersection = Sets.intersection(a, b).size();
        int union = Sets.union(a, b).size();
        return union == 0 ? 0.0 : (double) intersection / union;
    }

    /**
     * 计算种群中所有染色体两两之间的平均 Jaccard 相似度
     *
     * @param linesByChromosome 一个数组或列表，按索引存放每个染色体的覆盖行集合
     * @return 平均相似度 (0.0 ~ 1.0)
     */
    public static double averagePopulationSimilarity(List<Set<Integer>> linesByChromosome) {
        int n = linesByChromosome.size();
        if (n < 2) {
            return 0.0;
        }
        double total = 0.0;
        int count = 0;
        for (int i = 0; i < n; i++) {
            Set<Integer> lines1 = linesByChromosome.get(i);
            for (int j = i + 1; j < n; j++) {
                Set<Integer> lines2 = linesByChromosome.get(j);
                total += jaccardSimilarity(lines1, lines2);
                count++;
            }
        }
        return count == 0 ? 0.0 : total / count;
    }
}
