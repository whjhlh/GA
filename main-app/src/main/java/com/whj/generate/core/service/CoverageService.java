package com.whj.generate.core.service;

import com.whj.generate.core.domain.Chromosome;
import com.whj.generate.core.domain.Nature;
import com.whj.generate.core.domain.Population;

import java.util.Set;

/**
 * @author whj
 * @date 2025-05-19 下午12:50
 */
public interface CoverageService {
    /**
     * 计算某代种群的覆盖率
     * @param nature
     * @param population
     */
    void calculatePopulationCoverage(Nature nature, Population population) ;
    /**
     * 获取环境中上一代未覆盖行
     * @param nature
     * @return
     */
    Set<Integer> getLastPopulationUncovered(Nature nature);

    /**
     * 获取某一代未覆盖行
     * @param population
     * @return
     */
    Set<Integer> getPopulationUncovered(Population population);
}
