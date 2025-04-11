package com.whj.generate.core.service;

import com.whj.generate.core.domain.Population;

/**
 * @author whj
 * @date 2025-04-10 上午1:58
 */
public interface GeneticAlgorithmService {
    /**
     * 初始化环境
     * @param clazz
     * @param methodName
     * @return
     */
    Population initEnvironment(Class<?> clazz, String methodName);
}
