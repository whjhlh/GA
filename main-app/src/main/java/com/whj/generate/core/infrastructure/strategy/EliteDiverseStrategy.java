package com.whj.generate.core.infrastructure.strategy;

import com.whj.generate.biz.Infrastructure.cache.ChromosomeCoverageTracker;
import com.whj.generate.core.domain.Chromosome;
import com.whj.generate.core.domain.Nature;
import com.whj.generate.core.domain.Population;
import com.whj.generate.core.service.impl.FitnessCalculatorServiceImpl;
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
    private final FitnessCalculatorServiceImpl fitnessCalculator;
    private final ChromosomeCoverageTracker coverageTracker;


    @Autowired
    public EliteDiverseStrategy(FitnessCalculatorServiceImpl fitnessCalculator, ChromosomeCoverageTracker coverageTracker) {
        this.fitnessCalculator = fitnessCalculator;
        this.coverageTracker = coverageTracker;
    }

    /**
     * 获取适应度最高的30%的基因组
     *
     * @param nature
     * @param population
     * @return
     */
    @Override
    public List<Chromosome> select(Nature nature, Population population) {
        return population.getChromosomeSet().stream()
                .sorted((a, b) -> Double.compare(
                        fitnessCalculator.calculate(nature,population,b),
                        fitnessCalculator.calculate(nature, population, a)
                ))
                .limit((long) (population.getChromosomeSet().size() * 0.4))
                .collect(Collectors.toList());
    }
}

