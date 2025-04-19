package com.whj.generate.core.domain;

import org.jacoco.agent.rt.IAgent;
import org.jacoco.agent.rt.RT;

import java.util.*;

/**
 * @author whj
 * @date 2025-04-20 上午1:14
 */
public class Nature {
    private IAgent agent;
    private List<Population> populationList;
    private Map<Chromosome, byte[]> chromosomeCoverageDataMap;
    private Set<byte[]> methodCoverageDataSet;
    public Nature() {
        this.agent = RT.getAgent();
        this.populationList = new ArrayList<>();
        this.chromosomeCoverageDataMap = new HashMap<>();
        this.methodCoverageDataSet = new HashSet<>();
    }

    public IAgent getAgent() {
        return agent;
    }

    public void setAgent(IAgent agent) {
        this.agent = agent;
    }

    public List<Population> getPopulationList() {
        return populationList;
    }

    public void setPopulationList(List<Population> populationList) {
        this.populationList = populationList;
    }

    public Map<Chromosome, byte[]> getChromosomeCoverageDataMap() {
        return chromosomeCoverageDataMap;
    }

    public void setChromosomeCoverageDataMap(Map<Chromosome, byte[]> chromosomeCoverageDataMap) {
        this.chromosomeCoverageDataMap = chromosomeCoverageDataMap;
    }

    public Set<byte[]> getMethodCoverageDataSet() {
        return methodCoverageDataSet;
    }

    public void setMethodCoverageDataSet(Set<byte[]> methodCoverageDataSet) {
        this.methodCoverageDataSet = methodCoverageDataSet;
    }

    public void addPopulation(Population population) {
        this.populationList.add(population);
    }

}
