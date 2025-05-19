package com.whj.generate.core.infrastructure.strategy;

import com.whj.generate.core.domain.GenePool;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 基因初始化分发策略
 * @author whj
 * @date 2025-04-09 下午11:57
 */
public interface CombinationStrategy {
    Object[] generateCombination();
    void resetUsageTracking();
}
