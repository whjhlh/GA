package com.whj.generate.core.service;

import com.whj.generate.core.domain.Nature;
import com.whj.generate.core.domain.Population;

/**
 * @author whj
 * @date 2025-05-19 下午12:50
 */
public interface CoverageService {
    void calculatePopulationCoverage(Nature nature, Population population) ;
}
