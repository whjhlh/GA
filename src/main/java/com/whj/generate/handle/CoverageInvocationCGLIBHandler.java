package com.whj.generate.handle;

import com.whj.generate.utill.JaCocoUtil;
import com.whj.generate.utill.ReflectionUtil;
import org.jacoco.core.data.ExecutionDataStore;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * CGLIB 方法拦截器（线程安全，支持代理类识别）
 *
 * @author whj
 * @date 2025-02-22 下午8:09
 */
public class CoverageInvocationCGLIBHandler implements MethodInterceptor {
    private final Object target; // 持有真实目标实例

    public CoverageInvocationCGLIBHandler(Object target) {
        this.target = target;
    }

    @Override
    public Object intercept(Object proxyObj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        System.out.println("[代理] 调用方法: " + method.getName());
        // 1. 获取原始目标类（绕过代理类）
        Class<?> targetClass = ReflectionUtil.getTargetClass(proxyObj);
        // 跳过 toString() 的覆盖逻辑
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(target, args);
        }
        // 2. 开始记录覆盖率
        JaCocoUtil.startRecording();
        ExecutionDataStore before = JaCocoUtil.getCurrentRecording(); // 初始化基准数据

        Object result;
        try {
            // 3. 执行原始方法
            System.out.println("[代理] 调用方法: " + method.getName());
            result = proxy.invokeSuper(proxyObj, args);
        } finally {
            // 4. 确保异常时仍计算覆盖率
            ExecutionDataStore after = JaCocoUtil.stopRecording();
            double coverage = JaCocoUtil.analyzeCoverage(targetClass, before, after);
            JaCocoUtil.logCoverage(targetClass, method, coverage);
        }
        JaCocoUtil.cleanRecord();
        return result;
    }

}