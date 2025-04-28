package com.whj.generate.core.infrastructure.strategy;

import com.whj.generate.biz.Infrastructure.cache.ChromosomeCoverageTracker;
import com.whj.generate.core.domain.Chromosome;
import com.whj.generate.core.domain.Population;
import com.whj.generate.core.service.FitnessCalculatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 适应度排序+多样性
 * @author whj
 * @date 2025-04-26 下午12:51
 */
@Component
@Qualifier("eliteDiverseStrategy")
public class EliteDiverseStrategy implements SelectionStrategy {
    private final FitnessCalculatorService fitnessCalculator;

    @Autowired
    public EliteDiverseStrategy(FitnessCalculatorService fitnessCalculator) {
        this.fitnessCalculator = fitnessCalculator;
    }

    /**
     * 获取适应度最高的30%的基因组
     * @param population
     * @param tracker
     * @return
     */
    @Override
    public List<Chromosome> select(Population population, ChromosomeCoverageTracker tracker) {
        Set<Integer> uncovered = tracker.getUncoveredLines(population.getTargetMethod());
        return population.getChromosomeSet().stream()
                .sorted((a, b) -> Double.compare(
                        fitnessCalculator.calculate(b, tracker, uncovered),
                        fitnessCalculator.calculate(a, tracker, uncovered)
                ))
                .limit((long) (population.getChromosomeSet().size() * 0.3))
                .collect(Collectors.toList());
    }
}

