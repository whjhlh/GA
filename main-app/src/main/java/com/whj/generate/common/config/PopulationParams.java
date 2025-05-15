package com.whj.generate.common.config;

/**
 * @author whj
 * @date 2025-05-15 下午4:15
 */
public class PopulationParams {
    // 核心进化参数
    private Double crossoverRate;
    private Double mutationRate;

    // 终止条件参数
    private Integer maxGenerationCount;
    private Integer targetCoverage;

    // 权重参数
    private Double noveltyWeight;
    private Double diversityPenalty;
    private Double baseWeight;

    // 时间戳
    private Long timestamp;

    // 复制当前配置的工厂方法
    public static PopulationParams snapshot() {
        PopulationParams params = new PopulationParams();
        params.crossoverRate = GeneticAlgorithmConfig.CROSSOVER_RATE;
        params.mutationRate = GeneticAlgorithmConfig.MUTATION_RATE;
        params.maxGenerationCount = GeneticAlgorithmConfig.MAX_GENERATION_COUNT;
        params.targetCoverage = GeneticAlgorithmConfig.TARGET_COVERAGE;
        params.noveltyWeight = GeneticAlgorithmConfig.NOVELTY_WEIGHT;
        params.diversityPenalty = GeneticAlgorithmConfig.DIVERSITY_PENALTY;
        params.baseWeight = GeneticAlgorithmConfig.BASE_WEIGHT;
        params.timestamp = System.currentTimeMillis();
        return params;
    }
}