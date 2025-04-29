package com.whj.generate.core.service;

import com.google.common.collect.Sets;
import com.whj.generate.biz.Infrastructure.cache.ChromosomeCoverageTracker;
import com.whj.generate.core.domain.Chromosome;
import org.springframework.stereotype.Service;

import java.util.Set;

import static com.whj.generate.common.config.GeneticAlgorithmConfig.*;

/**
 * @author whj
 * @date 2025-04-26 下午12:57
 */
@Service
public class FitnessCalculatorService {

    /**
     * 计算适应度
     *
     * @param chromosome
     * @param tracker
     * @param uncovered
     * @return
     */
    public double calculate(Chromosome chromosome, ChromosomeCoverageTracker tracker, Set<Integer> uncovered) {
        // 基础适应度（加入权重系数）
        double baseFitness = chromosome.getCoveragePercent() * BASE_WEIGHT;

        // 新行覆盖奖励：使用精确的未覆盖行集合
        int novelty = tracker.getNewLinesCovered(chromosome, uncovered).size();
        double noveltyReward = novelty * 100.0 * NOVELTY_WEIGHT;

        // 多样性惩罚：考虑与其他染色体的相似性
        double similarityPenalty = calculateSimilarityPenalty(chromosome, tracker) * DIVERSITY_PENALTY;

        // 最终适应度（确保非负）
        double fitness = Math.max(baseFitness + noveltyReward - similarityPenalty, 0);
        chromosome.setFitness((long) fitness);
        return chromosome.getFitness();
    }

    /**
     * 计算相似性惩罚
     *
     * @param target
     * @param tracker
     * @return
     */
    private double calculateSimilarityPenalty(Chromosome target, ChromosomeCoverageTracker tracker) {
        Set<Chromosome> coveringChromosomes = tracker.getCoveringChromosomeSet(target);
        if (coveringChromosomes.isEmpty()) {
            return 0.0;
        }
        Set<Integer> targetLines = tracker.getLinesCoveredBy(target);
        return coveringChromosomes.parallelStream()
                .filter(c -> !c.equals(target)) // 使用equals确保正确过滤
                .mapToDouble(other -> {
                    Set<Integer> otherLines = tracker.getLinesCoveredBy(other);
                    return jaccardSimilarity(targetLines, otherLines);
                })
                .average()
                .orElse(0.0);
    }


    /**
     * 计算Jaccard相似度
     *
     * @param a
     * @param b
     * @return
     */
    private double jaccardSimilarity(Set<Integer> a, Set<Integer> b) {
        if (a.isEmpty() && b.isEmpty()) return 0;
        int intersection = Sets.intersection(a, b).size();
        int union = Sets.union(a, b).size();
        return (double) intersection / union;
    }
}