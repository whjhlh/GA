package com.whj.generate.common.dto;

import java.util.Set;

/**
 * @author whj
 * @date 2025-05-17 上午11:26
 */
public class CoveredDTO {
    Set<Integer> coveredLine;
    Object[] genes;

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
