package com.whj.generate.common.response;

import com.whj.generate.common.dto.ChromosomeDTO;

import java.util.List;

/**
 * @author whj
 * @date 2025-05-06 上午1:22
 */
public class PopulationResponse {
    private final String sessionId;
    private final int generationIndex;
    private final double currentCoverage;
    private final int populationSize;
    private final List<ChromosomeDTO> chromosomes;

    public PopulationResponse(String sessionId, int generationIndex, double currentCoverage,
                              int populationSize, List<ChromosomeDTO> chromosomes) {
        this.sessionId = sessionId;
        this.generationIndex = generationIndex;
        this.currentCoverage = currentCoverage;
        this.populationSize = populationSize;
        this.chromosomes = chromosomes;
    }

    public String getSessionId() {
        return sessionId;
    }

    public int getGenerationIndex() {
        return generationIndex;
    }

    public double getCurrentCoverage() {
        return currentCoverage;
    }

    public int getPopulationSize() {
        return populationSize;
    }

    public List<ChromosomeDTO> getChromosomes() {
        return chromosomes;
    }
}
