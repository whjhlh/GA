package com.whj.generate.core.service.impl;

/**
 * @author whj
 * @date 2025-05-19 下午4:44
 */

import com.whj.generate.core.domain.GenePool;
import com.whj.generate.core.infrastructure.PoolLoader;
import com.whj.generate.core.service.GenPoolService;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;

/**
 * 基因池服务
 *
 * @author whj
 * @date 2025-05-19 下午3:38
 */
@Service
public class GenPoolServiceImpl implements GenPoolService {
    private final PoolLoader<GenePool> poolLoader;

    public GenPoolServiceImpl(PoolLoader<GenePool> poolLoader) {
        this.poolLoader = poolLoader;
    }

    @Override
    public GenePool initGenePool(Class<?> clazz, Method method) {
        return poolLoader.initializePool(clazz, method);
    }
}

