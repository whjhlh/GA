package com.whj.generate.core.service;

import com.whj.generate.biz.Infrastructure.cache.ChromosomeCoverageTracker;
import com.whj.generate.core.domain.Nature;
import com.whj.generate.core.domain.Population;

/**
 * @author whj
 * @date 2025-04-10 上午1:58
 */
public interface GeneticAlgorithmService {
    /**
     *种群初始化
     *1.根据方法入参和方法内条件获取基因库
     *2.根据基因库初始化种群
     *3.计算种群内每个个体的适应度值
     * @return
     */
    Population initEnvironment(Nature nature, Class<?> clazz, String methodName);

    /**
     * 种群进化
     * @param nature
     * @param count
     * @return
     */
    Population evolvePopulation(Nature nature,Integer count);

    /**
     * 获取行覆盖信息
     * @return
     */
    ChromosomeCoverageTracker getCoverageTracker();
}
