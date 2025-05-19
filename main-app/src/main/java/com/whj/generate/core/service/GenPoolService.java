package com.whj.generate.core.service;

import com.whj.generate.core.domain.GenePool;

import java.lang.reflect.Method;

/**
 * @author whj
 * @date 2025-05-19 下午3:39
 */
public interface GenPoolService {
     GenePool initGenePool(Class<?> clazz, Method method) ;
}
