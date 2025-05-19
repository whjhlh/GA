package com.whj.generate.core.domain;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serial;
import java.lang.reflect.Method;

/**
 * @author whj
 * @date 2025-05-19 下午4:54
 */
public class PopulationBase {
    /**
     * 序列化
     */
    @Serial
    private static final long serialVersionUID = 128714233634L;
    /**
     * 目标测试类
     */
    @JSONField(serialize = false)
    private final Class<?> targetClass;

    /**
     * 目标方法
     */
    @JSONField(serialize = false)
    private final Method method;


    public PopulationBase(Class<?> targetClass, Method method) {
        this.targetClass = targetClass;
        this.method = method;
    }

    public Class<?> getTargetClass() {
        return targetClass;
    }

    public Method getMethod() {
        return method;
    }
}
