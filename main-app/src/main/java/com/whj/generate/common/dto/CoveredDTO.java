package com.whj.generate.common.dto;

import java.util.Set;

/**
 * @author whj
 * @date 2025-05-17 上午11:26
 */
public class CoveredDTO {
    /**
     * 染色体序号
     */
    String chromosomeId;
    /**
     * 覆盖行
     */
    Set<Integer> coveredLine;
    /**
     * 基因
     */
    Object[] genes;

    public String getChromosomeId() {
        return chromosomeId;
    }

    public void setChromosomeId(String chromosomeId) {
        this.chromosomeId = chromosomeId;
    }

    public Set<Integer> getCoveredLine() {
        return coveredLine;
    }

    public void setCoveredLine(Set<Integer> coveredLine) {
        this.coveredLine = coveredLine;
    }

    public Object[] getGenes() {
        return genes;
    }

    public void setGenes(Object[] genes) {
        this.genes = genes;
    }
}
