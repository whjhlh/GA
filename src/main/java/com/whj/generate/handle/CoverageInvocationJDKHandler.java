package com.whj.generate.handle;

import com.whj.generate.utill.JaCocoUtil;
import org.jacoco.core.data.ExecutionDataStore;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * JDK代理方法拦截器
 * @author whj
 * @date 2025-02-23 上午1:02
 */
public class CoverageInvocationJDKHandler implements InvocationHandler {
    @Override
    public Object invoke(Object target, Method method, Object[] args) throws Throwable {
        ExecutionDataStore coverageBefore = JaCocoUtil.getCurrentCoverage();
        Object result = method.invoke(target, args);
        ExecutionDataStore coverageAfter = JaCocoUtil.getCurrentCoverage();
        JaCocoUtil.analyzeCoverage(target.getClass(), coverageBefore, coverageAfter);
        return result;
    }
}
