package com.whj.generate.biz.service;

import com.whj.generate.biz.application.FitnessCalculator;
import com.whj.generate.core.domain.Population;

/**
 * 种群适应度计算服务
 * @author whj
 * @date 2025-04-18 上午1:14
 */
public class GeneticAlgorithmAppService {
    private final FitnessCalculator fitnessCalculator;

    public GeneticAlgorithmAppService(FitnessCalculator fitnessCalculator) {
        this.fitnessCalculator = fitnessCalculator;
    }

    public void calculatePopulationFitness(Population population) {
        population.getChromosomes().forEach(chromosome -> {
            double fitness = fitnessCalculator.calculateFitness(
                    chromosome.getMethod(),
                    chromosome.getGenes()
            );
            chromosome.setFitness(fitness);
        });
    }
}
