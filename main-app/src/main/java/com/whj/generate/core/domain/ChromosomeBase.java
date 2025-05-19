package com.whj.generate.core.domain;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * @author whj
 * @date 2025-05-19 下午4:48
 */
public class ChromosomeBase implements Serializable {

    /**
     * 序列化
     */
    @Serial
    private static final long serialVersionUID = 124238713634L;
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

    public ChromosomeBase(Class<?> targetClass, Method method) {
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
