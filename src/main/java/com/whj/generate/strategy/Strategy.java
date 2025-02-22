package com.whj.generate.strategy;

/**
 * 策略接口
 *
 * @author whj
 * @date 2025-01-01 下午7:09
 */
public interface Strategy {
    String generateTestCase(Class<?> clazz);
}

