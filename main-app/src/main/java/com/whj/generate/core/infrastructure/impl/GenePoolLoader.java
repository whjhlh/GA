package com.whj.generate.core.infrastructure.impl;


import com.whj.generate.core.domain.GenePool;
import com.whj.generate.core.infrastructure.ParamThresholdExtractor;
import com.whj.generate.core.infrastructure.PoolLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 基因池加载器
 * @author whj
 * @date 2025-04-09 上午12:08
 */
@Component
public class GenePoolLoader implements PoolLoader<GenePool> {

    private final ParamThresholdExtractor thresholdExtractor;
    @Autowired
    public GenePoolLoader(ParamThresholdExtractor thresholdExtractor) {
        this.thresholdExtractor = thresholdExtractor;
    }

    @Override
    public GenePool initializePool(Class<?> targetClass, Method method){
        Map<String, Set<Object>> geneticMap = thresholdExtractor.extractThresholds(targetClass, method.getName());
        List<String> paramsList = thresholdExtractor.resolveParameterNames(method);
        int paramIndex = 0;

        GenePool genePool = new GenePool();

        //根据参数名，将基因位加载入基因池
        for(String param : paramsList){
            Object[] genes = geneticMap.get(param).toArray();
            genePool.loadGenes(paramIndex++, genes);
        }
        return genePool;
    }
}
