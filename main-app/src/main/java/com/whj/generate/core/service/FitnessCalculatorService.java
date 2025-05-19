package com.whj.generate.core.service;

import com.whj.generate.biz.Infrastructure.cache.ChromosomeCoverageTracker;
import com.whj.generate.core.domain.Chromosome;
import com.whj.generate.utill.SimilarityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

import static com.whj.generate.common.config.GeneticAlgorithmConfig.*;

/**
 * @author whj
 * @date 2025-04-26 下午12:57
 */
@Service
public class FitnessCalculatorService {
    private final ChromosomeCoverageTracker coverageTracker;
@Autowired
    public FitnessCalculatorService(ChromosomeCoverageTracker coverageTracker) {
        this.coverageTracker = coverageTracker;
    }

    /**
     * 计算适应度
     *
     * @param chromosome
     * @param uncovered
     * @return
     */
    public double calculate(Chromosome chromosome, Set<Integer> uncovered) {
        // 基础适应度（加入权重系数）
        double baseFitness = chromosome.getCoveragePercent() * BASE_WEIGHT;

        // 新行覆盖奖励：使用精确的未覆盖行集合
        int novelty = coverageTracker.getNewLinesCovered(chromosome, uncovered).size();
        double noveltyReward = novelty * 100.0 * NOVELTY_WEIGHT;

        // 多样性惩罚：考虑与其他染色体的相似性
        double similarityPenalty = calculateSimilarityPenalty(chromosome) * DIVERSITY_PENALTY;

        // 最终适应度（确保非负）
        double fitness = Math.max(baseFitness + noveltyReward - similarityPenalty, 0);
        chromosome.setFitness((long) fitness);
        return chromosome.getFitness();
    }

    /**
     * 计算相似性惩罚
     *
     * @param target
     * @return
     */
    private double calculateSimilarityPenalty(Chromosome target) {
        Set<Chromosome> coveringChromosomes = coverageTracker.getCoveringChromosomeSet(target);
        if (coveringChromosomes.isEmpty()) {
            return 0.0;
        }
        Set<Integer> targetLines = coverageTracker.getLinesCoveredBy(target);
        return coveringChromosomes.parallelStream()
                .filter(c -> !c.equals(target)) // 使用equals确保正确过滤
                .mapToDouble(other -> {
                    Set<Integer> otherLines = coverageTracker.getLinesCoveredBy(other);
                    return SimilarityUtils.jaccardSimilarity(targetLines, otherLines);
                })
                .average()
                .orElse(0.0);
    }

}