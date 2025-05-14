package com.whj.generate.core.infrastructure;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @description: 参数阈值提取器
 * @author whj
 * @date 2025-04-09 上午12:12
 */
public interface ParamThresholdExtractor {
    /**
     * 提取参数阈值
     * @param targetClass
     * @param methodName
     * @return
     * @throws Exception
     */
    Map<String, Set<Object>> extractThresholds(Class<?> targetClass,String methodName) ;

    /**
     * 解析参数名
     *
     * @param method
     * @return
     */
    List<String> resolveParameterNames(Method method);

}
