package com.whj.generate.core.infrastructure;


import com.whj.generate.core.domain.GenePool;
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
public class GeneLoader {
    @Autowired
    private final ConditionExtractor thresholdExtractor;

    public GeneLoader(ConditionExtractor thresholdExtractor) {
        this.thresholdExtractor = thresholdExtractor;
    }

    public GenePool loadGenePool(Class<?> targetClass, Method method){
        GenePool genePool = new GenePool();
        Map<String, Set<Object>> geneticMap = thresholdExtractor.extractThresholds(targetClass, method.getName());

        int paramIndex = 0;
        List<String> paramsConst = ConditionExtractor.getParamsConst();
        for(String param : paramsConst){
            genePool.loadGenes(paramIndex++, geneticMap.get(param).toArray());
        }
        for (Map.Entry<String, Set<Object>> entry : geneticMap.entrySet()) {
            genePool.loadGenes(paramIndex++, entry.getValue().toArray());
        }
        return genePool;
    }
}
