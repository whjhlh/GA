package com.whj.generate.common.response;

/**
 * @author whj
 * @date 2025-05-06 上午12:53
 */

public class EvolveResponse {
    private String sessionId;
    private int nextGeneration;
    private double coverage;
    private int populationSize;
    private boolean finished;

    public EvolveResponse(String sessionId, int nextGeneration, double coverage, int populationSize, boolean finished) {
        this.sessionId = sessionId;
        this.nextGeneration = nextGeneration;
        this.coverage = coverage;
        this.populationSize = populationSize;
        this.finished = finished;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public int getNextGeneration() {
        return nextGeneration;
    }

    public void setNextGeneration(int nextGeneration) {
        this.nextGeneration = nextGeneration;
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

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }
// getters omitted
}