package com.whj.generate.common.config;

/**
 * @author whj
 * @description: 遗传算法配置信息
 * @date 2025-04-20 上午3:11
 */
public class GeneticAlgorithmConfig {
    /**
     * 初始化阶段
     */
    public static final String INIT_PHASE = "init";
    /**
     * 交叉概率
     */
    public static double CROSSOVER_RATE = 0.5;
    /**
     * 变异概率
     */
    public static double MUTATION_RATE = 0.70;
    /**
     * 最大迭代次数
     */
    public static int MAX_GENERATION_COUNT = 10;
    /**
     * 覆盖率要求
     */
    public static int TARGET_COVERAGE = 90;
    /**
     * 新行覆盖奖励权重
     */
    public static double NOVELTY_WEIGHT = 0.8;
    /**
     * 多样性惩罚权重
     */
    public static double DIVERSITY_PENALTY = 0.9;
    /**
     * 基础适应度权重
     */
    public static double BASE_WEIGHT = 0.5;

}
