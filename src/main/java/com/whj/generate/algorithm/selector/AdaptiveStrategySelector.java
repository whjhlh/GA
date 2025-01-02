package com.whj.generate.algorithm.selector;

import com.whj.generate.algorithm.strategy.GeneticAlgorithmStrategy;
import com.whj.generate.algorithm.strategy.SimulatedAnnealingStrategy;
import com.whj.generate.algorithm.strategy.Strategy;
import com.whj.generate.model.MethodStructure;

/**
 * 策略选择器
 *
 * @author whj
 * @date 2025-01-01 下午7:09
 */
public class AdaptiveStrategySelector {
    public static Strategy selectStrategy(MethodStructure method) {
        // 根据方法参数数量选择策略（可扩展为复杂条件）
        if (method.getParameters().split(",").length > 2) {
            return new GeneticAlgorithmStrategy();
        } else {
            return new SimulatedAnnealingStrategy();
        }
    }
}
