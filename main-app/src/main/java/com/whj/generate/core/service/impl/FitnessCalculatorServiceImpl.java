package com.whj.generate.core.service.impl;

import com.whj.generate.biz.Infrastructure.cache.ChromosomeCoverageTracker;
import com.whj.generate.core.domain.ChromosomeBase;
import com.whj.generate.core.domain.Chromosome;
import com.whj.generate.core.domain.Nature;
import com.whj.generate.core.domain.Population;
import com.whj.generate.core.service.FitnessCalculatorService;
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
public class FitnessCalculatorServiceImpl implements FitnessCalculatorService {
    /**
     * 覆盖追踪器
     */
    private final ChromosomeCoverageTracker coverageTracker;

    @Autowired
    public FitnessCalculatorServiceImpl(ChromosomeCoverageTracker coverageTracker) {
        this.coverageTracker = coverageTracker;
    }

    /**
     * 计算适应度并更新染色体适应度
     *
     * @param
     * @param nature
     * @param population
     * @return
     */
    public double calculate(Nature nature, Population population, ChromosomeBase baseChromosome) {
        if(baseChromosome instanceof Chromosome chromosome){
            // 基础适应度（加入权重系数）
            double baseFitness = chromosome.getCoveragePercent() * BASE_WEIGHT;

            // 新行覆盖奖励：使用精确的未覆盖行集合
            double noveltyReward = getNoveltyReward(nature, population, chromosome);

            // 多样性惩罚：考虑与其他染色体的相似性
            double similarityPenalty = calculateSimilarityPenalty(chromosome) * DIVERSITY_PENALTY;

            // 最终适应度（确保非负）
            double fitness = Math.max(baseFitness + noveltyReward - similarityPenalty, 0);
            chromosome.setFitness((long) fitness);
            return chromosome.getFitness();
        }
        return 0;
    }

    /**
     * 获取新行覆盖奖励
     *
     * @param nature
     * @param population
     * @param chromosome
     * @return
     */
    private double getNoveltyReward(Nature nature, Population population, Chromosome chromosome) {
        int novelty = coverageTracker.getNewCovered(nature, population, chromosome).size();
        return novelty * 100.0 * NOVELTY_WEIGHT;
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
        Set<Integer> targetLines = coverageTracker.getChromosomeCovered(target);
        return coveringChromosomes.parallelStream()
                .filter(c -> !c.equals(target)) // 使用equals确保正确过滤
                .mapToDouble(other -> {
                    Set<Integer> otherLines = coverageTracker.getChromosomeCovered(other);
                    return SimilarityUtils.jaccardSimilarity(targetLines, otherLines);
                })
                .average()
                .orElse(0.0);
    }

}