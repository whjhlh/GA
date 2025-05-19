package com.whj.generate.core.infrastructure.strategy;

import com.whj.generate.core.domain.Chromosome;
import com.whj.generate.core.domain.Nature;
import com.whj.generate.core.domain.Population;

import java.util.List;

/**
 * @author whj
 * @date 2025-04-26 下午12:51
 */
public interface SelectionStrategy {
    List<Chromosome> select(Nature nature, Population population);
}
