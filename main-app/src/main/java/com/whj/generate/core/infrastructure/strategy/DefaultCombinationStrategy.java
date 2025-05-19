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

    private Map<Integer, GeneSource> initializeGeneSources(GenePool pool) {
        return pool.getParameterIndexes().stream()
                .filter(Objects::nonNull)
                .peek(i -> {
                    if (i < 0) {
                        logger.warn("Negative parameter index detected: {}", i);
                    }
                })
                .collect(Collectors.toConcurrentMap(
                        i -> i,
                        i -> {
                            Object[] values = pool.getThresholdValues(i);
                            if (values == null || values.length == 0) {
                                logger.error("Empty genes for index {}", i);
                                return new GeneSource(new Object[0], ConcurrentHashMap.newKeySet());
                            }
                            return new GeneSource(values, ConcurrentHashMap.newKeySet());
                        },
                        (existing, replacement) -> existing
                ));
    }

    public DefaultCombinationStrategy(GenePool genePool) {
        Objects.requireNonNull(genePool, "GenePool cannot be null");
        this.geneSources = initializeGeneSources(genePool);

        if (geneSources.isEmpty()) {
            logger.error("Empty gene sources initialized");
            throw new IllegalArgumentException("No valid parameter indexes found");
        }
    }

    @Override
    public Object[] generateCombination() {
        Object[] combination = new Object[geneSources.size()];

        geneSources.forEach((index, source) -> {
            if (index >= combination.length) {
                logger.error("Index {} exceeds combination size {}", index, combination.length);
                return;
            }

            Optional<Object> candidate = Arrays.stream(source.availableGenes)
                    .filter(gene -> !source.usedGenes.contains(gene))
                    .findFirst();

            combination[index] = candidate.orElseGet(() ->
                    source.availableGenes[ThreadLocalRandom.current().nextInt(source.availableGenes.length)]
            );

            if (combination[index] == null) {
                logger.warn("Selected null gene at index {}", index);
            }

            source.usedGenes.add(combination[index]);
        });

        return combination;
    }

    @Override
    public void resetUsageTracking() {

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