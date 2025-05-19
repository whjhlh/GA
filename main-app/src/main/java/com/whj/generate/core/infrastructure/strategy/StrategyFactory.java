package com.whj.generate.core.infrastructure.strategy;

import com.whj.generate.core.domain.GenePool;

/**
 * @author whj
 * @date 2025-05-19 下午11:48
 */
public interface StrategyFactory {
    CombinationStrategy createStrategy(GenePool genePool);
}