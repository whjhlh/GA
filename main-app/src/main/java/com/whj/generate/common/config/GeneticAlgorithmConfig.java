package com.whj.generate.common.config;

/**
 * @description: 遗传算法配置信息
 * @author whj
 * @date 2025-04-20 上午3:11
 */
public class GeneticAlgorithmConfig {
    /**
     * 交叉概率
     */
    public static final double CROSSOVER_RATE = 0.5;
    /**
     * 变异概率
     */
    public static final double MUTATION_RATE = 0.70;
    public static final String INIT_PHASE = "init";
    /**
     * 最大迭代次数
     */
    public static final int MAX_GENERATION_COUNT = 10;
    /**
     * 覆盖率要求
     */
    public static final int TARGET_COVERAGE = 90;
    /**
     * 新行覆盖奖励权重
     */
    public static final double NOVELTY_WEIGHT = 0.8;
    /**
     * 多样性惩罚权重
     */
    public static final double DIVERSITY_PENALTY = 0.9;
    /**
     * 基础适应度权重
     */
    public static final double BASE_WEIGHT = 0.5;
}
