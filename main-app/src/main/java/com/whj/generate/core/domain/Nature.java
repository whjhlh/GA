package com.whj.generate.core.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * @author whj
 * @description:
 * @date 2025-04-20 上午1:14
 */
public class Nature {
    /**
     * 历史种群
     */
    private List<Population> populationList;

    public Nature() {
        this.populationList = new ArrayList<>();
    }

    public List<Population> getPopulationList() {
        return populationList;
    }

    public void setPopulationList(List<Population> populationList) {
        this.populationList = populationList;
    }

    public void addPopulation(Population population) {
        this.populationList.add(population);
    }

    public boolean hasPopulation() {
        return populationList.isEmpty();
    }
}
