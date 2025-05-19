package com.whj.generate.core.service;

import com.whj.generate.core.domain.Population;

import java.lang.reflect.Method;

/**
 * @author whj
 * @date 2025-05-20 上午12:22
 */
public interface GenerateService {
    /**
     * 创建种群
     * @param clazz
     * @param method
     * @return
     */
    Population genertatePopulation(Class<?> clazz, Method method);
}
