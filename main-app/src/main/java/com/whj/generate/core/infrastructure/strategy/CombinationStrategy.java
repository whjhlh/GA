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
public class CombinationStrategy {
    /**
     * 基因池
     */
    private final GenePool genePool;
    /**
     * 已使用的基因记录
     */
    private final Map<Integer, Set<Object>> usedGeneMap = new ConcurrentHashMap<>();

    public CombinationStrategy(GenePool genePool) {
        this.genePool = genePool;
        // 初始化使用记录
        genePool.getParameterIndexes().forEach(i ->
                usedGeneMap.put(i, Collections.newSetFromMap(new ConcurrentHashMap<>()))
        );
    }

    /**
     * 生成保证最小覆盖的组合
     */
    public Object[] generateMinimalCombination() {
        Object[] combination = new Object[genePool.getParameterCount()];

        // 优先填充未使用过的基因值
        for (int i = 0; i < combination.length; i++) {
            Object[] available = genePool.getThresholdValues(i);

            // 找出第一个未使用的基因值
            int finalI = i;
            Optional<Object> unused = Arrays.stream(available)
                    .filter(g -> !usedGeneMap.get(finalI).contains(g))
                    .findFirst();

            combination[i] = unused.orElseGet(() ->
                    available[ThreadLocalRandom.current().nextInt(available.length)]
            );

            usedGeneMap.get(i).add(combination[i]);
        }

        return combination;
    }
}
