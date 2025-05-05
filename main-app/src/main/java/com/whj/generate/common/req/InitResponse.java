package com.whj.generate.common.req;

/**
 * @author whj
 * @date 2025-05-06 上午12:51
 */
public class InitResponse {
    private String sessionId;
    private int generationIndex;
    private double coverage;
    private int populationSize;

    public InitResponse(String sessionId, int generationIndex, double coverage, int populationSize) {
        this.sessionId = sessionId;
        this.generationIndex = generationIndex;
        this.coverage = coverage;
        this.populationSize = populationSize;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public int getGenerationIndex() {
        return generationIndex;
    }

    public void setGenerationIndex(int generationIndex) {
        this.generationIndex = generationIndex;
    }

    public double getCoverage() {
        return coverage;
    }

    public void setCoverage(double coverage) {
        this.coverage = coverage;
    }

    public int getPopulationSize() {
        return populationSize;
    }

    public void setPopulationSize(int populationSize) {
        this.populationSize = populationSize;
    }
// getters omitted
}