package com.whj.generate.biz.application;

import java.lang.reflect.Method;

/**
 * @author whj
 * @date 2025-04-18 上午1:13
 */
public interface FitnessCalculator {
    double calculateFitness(Method method, Object[] params);
}
