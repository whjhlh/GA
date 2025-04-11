package com.whj.generate.core.infrastructure;

import com.whj.generate.core.domain.BasePool;

import java.lang.reflect.Method;

/**
 * @author whj
 * @date 2025-04-10 上午2:08
 */
public interface PoolLoader<T extends BasePool> {
    T initializePool(Class<?> targetClass, Method method);
}