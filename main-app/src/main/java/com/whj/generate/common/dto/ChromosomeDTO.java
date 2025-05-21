package com.whj.generate.common.dto;

/**
 * @author whj
 * @date 2025-05-06 上午1:23
 */
public class ChromosomeDTO {
    private final String id;
    private final String sequence;
    private final double coveragePercent;
    private final double fitness;
    private final Object genes;
    private final String errorMsg;

    public ChromosomeDTO(String id, String sequence, double coveragePercent,
                         double fitness, Object genes, String errorMsg) {
        this.id = id;
        this.sequence = sequence;
        this.coveragePercent = coveragePercent;
        this.fitness = fitness;
        this.genes = genes;
        this.errorMsg = errorMsg;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public String getId() {
        return id;
    }

    public String getSequence() {
        return sequence;
    }

    public double getCoveragePercent() {
        return coveragePercent;
    }

    public double getFitness() {
        return fitness;
    }

    public Object getGenes() {
        return genes;
    }
}
