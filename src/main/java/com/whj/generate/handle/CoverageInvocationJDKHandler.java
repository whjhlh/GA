package com.whj.generate.handle;

import com.whj.generate.utill.JaCocoUtil;
import org.jacoco.core.data.ExecutionDataStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * JDK动态代理拦截器（支持原始类识别、线程安全、返回值透传）
 *
 * @author whj
 * @date 2025-02-23 上午1:02
 */
public class CoverageInvocationJDKHandler implements InvocationHandler {
    private static final Logger logger = LoggerFactory.getLogger(CoverageInvocationJDKHandler.class);
    private final Object target; // 原始目标对象

    public CoverageInvocationJDKHandler(Object target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 1. 获取原始类（非代理接口）
        Class<?> targetClass = getTargetClass();

        // 2. 初始化覆盖率记录
        JaCocoUtil.startRecording();
        ExecutionDataStore before = JaCocoUtil.stopRecording();

        Object result;
        try {
            // 3. 执行原始方法
            result = method.invoke(target, args);
        } finally {
            // 4. 确保异常时仍计算覆盖率
            ExecutionDataStore after = JaCocoUtil.stopRecording();
            try {
                double coverage = JaCocoUtil.analyzeCoverage(targetClass, before, after);
                JaCocoUtil.logCoverage(targetClass, method, coverage);
            } catch (Exception e) {
                logger.error("覆盖率分析失败: {}", e.getMessage());
            }
        }

        return result; // 透传原始返回值
    }

    /**
     * 获取原始目标类（处理JDK代理接口问题）
     */
    private Class<?> getTargetClass() {
        // 如果代理的是接口，直接返回实现类的 Class
        return target.getClass().isInterface() ? target.getClass() : target.getClass().getSuperclass();
    }
}