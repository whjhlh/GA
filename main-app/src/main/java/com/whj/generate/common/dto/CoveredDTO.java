package com.whj.generate.common.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Set;
import java.util.TreeSet;

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
    @JsonSerialize(as = TreeSet.class)
    Set<Integer> coveredLine=new TreeSet<>();
    /**
     * 未覆盖行
     */
    @JsonSerialize(as = TreeSet.class)
    Set<Integer> unCoveredLine =new TreeSet<>();
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
