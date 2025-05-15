package com.whj.generate.common.dto;

/**
 * @author whj
 * @date 2025-05-15 下午4:59
 */
public class PopulationStatDTO {
    private int generation;
    private double maxFitness;
    private double avgFitness;
    private long coverage;
    private double crossoverRate;
    private double mutationRate;

    public int getGeneration() {
        return generation;
    }

    public void setGeneration(int generation) {
        this.generation = generation;
    }

    public double getMaxFitness() {
        return maxFitness;
    }

    public void setMaxFitness(double maxFitness) {
        this.maxFitness = maxFitness;
    }

    public double getAvgFitness() {
        return avgFitness;
    }

    public void setAvgFitness(double avgFitness) {
        this.avgFitness = avgFitness;
    }

    public long getCoverage() {
        return coverage;
    }

    public void setCoverage(long coverage) {
        this.coverage = coverage;
    }

    public double getCrossoverRate() {
        return crossoverRate;
    }

    public void setCrossoverRate(double crossoverRate) {
        this.crossoverRate = crossoverRate;
    }

    public double getMutationRate() {
        return mutationRate;
    }

    public void setMutationRate(double mutationRate) {
        this.mutationRate = mutationRate;
    }
}
