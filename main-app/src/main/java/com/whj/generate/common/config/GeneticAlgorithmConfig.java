package com.whj.generate.common.config;

/**
 * @description: 遗传算法配置信息
 * @author whj
 * @date 2025-04-20 上午3:11
 */
public class GeneticAlgorithmConfig {
    public static final int MAX_GEN = 100;
    public static final double CROSSOVER_RATE = 0.3;
    public static final double MUTATION_RATE = 0.70;
    public static final int ELITE_COUNT = 40;
    public static final String INIT_PHASE = "init";
    public static final int MAX_GENERATION_COUNT = 10;
    public static final int TARGET_COVERAGE = 90;
}
