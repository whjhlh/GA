package com.whj.generate.handle;

import com.whj.generate.utill.JaCocoUtil;
import org.jacoco.core.data.ExecutionDataStore;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;


/**
 * cglib代理方法拦截器
 * @author whj
 * @date 2025-02-22 下午8:09
 */
public class CoverageInvocationCGLIBHandler implements MethodInterceptor {
    @Override
    public Object intercept(Object target, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        ExecutionDataStore coverageBefore = JaCocoUtil.getCurrentCoverage();
        Object result = method.invoke(target, objects);
        ExecutionDataStore coverageAfter = JaCocoUtil.getCurrentCoverage();
        JaCocoUtil.analyzeCoverage(target.getClass(), coverageBefore, coverageAfter);
        return result;
    }
}
