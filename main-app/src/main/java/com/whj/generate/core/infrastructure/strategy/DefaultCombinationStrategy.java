package com.whj.generate.core.infrastructure.strategy;

import com.whj.generate.core.domain.GenePool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * @author whj
 * @date 2025-05-20 上午12:02
 */
public class DefaultCombinationStrategy implements CombinationStrategy {
    private static final Logger logger = LoggerFactory.getLogger(DefaultCombinationStrategy.class);
    private final Map<Integer, GeneSource> geneSources;

    public DefaultCombinationStrategy(GenePool genePool) {
        Objects.requireNonNull(genePool, "GenePool cannot be null");
        this.geneSources = initializeGeneSources(genePool);
        if (geneSources.isEmpty()) {
            logger.error("Empty gene sources initialized");
            throw new IllegalArgumentException("No valid parameter indexes found");
        }
    }

    private Map<Integer, GeneSource> initializeGeneSources(GenePool pool) {
        return pool.getParameterIndexes().stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        i -> i,
                        i -> {
                            Object[] values = pool.getThresholdValues(i);
                            if (values == null || values.length == 0) {
                                logger.error("Empty genes for index {}", i);
                                return new GeneSource(new Object[0], ConcurrentHashMap.newKeySet());
                            }
                            return new GeneSource(values, ConcurrentHashMap.newKeySet());
                        }
                ));
    }

    @Override
    public Object[] generateCombination() {
        Object[] combination = new Object[geneSources.size()];
        List<Integer> indices = new ArrayList<>(geneSources.keySet());
        Collections.sort(indices); // 保持参数顺序一致性

        for (int pos = 0; pos < indices.size(); pos++) {
            int paramIndex = indices.get(pos);
            GeneSource source = geneSources.get(paramIndex);
            Object[] available = source.availableGenes;
            int len = available.length;
            if (len == 0) continue;

            // 随机起点遍历可用基因，优先使用未用的
            Object selected = null;
            int start = ThreadLocalRandom.current().nextInt(len);
            for (int i = 0; i < len; i++) {
                Object candidate = available[(start + i) % len];
                if (!source.usedGenes.contains(candidate)) {
                    selected = candidate;
                    break;
                }
            }

            // fallback 到随机选择
            if (selected == null) {
                selected = available[ThreadLocalRandom.current().nextInt(len)];
            }

            combination[pos] = selected;
            source.usedGenes.add(selected);
        }

        return combination;
    }

    @Override
    public void resetUsageTracking() {
        geneSources.values().forEach(source -> source.usedGenes.clear());
    }

    private static class GeneSource {
        final Object[] availableGenes;
        final Set<Object> usedGenes;

        GeneSource(Object[] availableGenes, Set<Object> usedGenes) {
            this.availableGenes = availableGenes;
            this.usedGenes = usedGenes;
        }
    }
}
