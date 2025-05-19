package com.whj.generate.core.domain;

import java.util.Set;

/**
 * @author whj
 * @date 2025-05-19 下午6:01
 */
public class Covered {
    /**
     * 染色体序号
     */
    String chromosomeId;
    /**
     * 覆盖行
     */
    Set<Integer> coveredLine;
    /**
     * 未覆盖行
     */
    Set<Integer> unCoveredLine;
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

    public Set<Integer> getUnCoveredLine() {
        return unCoveredLine;
    }

    public void setUnCoveredLine(Set<Integer> unCoveredLine) {
        this.unCoveredLine = unCoveredLine;
    }

    public Object[] getGenes() {
        return genes;
    }

    public void setGenes(Object[] genes) {
        this.genes = genes;
    }
}
