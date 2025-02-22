package com.whj.generate.handle;

import com.whj.generate.utill.JaCocoUtil;
import org.jacoco.core.data.ExecutionDataStore;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;


/**
 * @author whj
 * @date 2025-02-22 下午8:09
 */
public class CoverageInvocationHandler implements InvocationHandler {
    /**
     * 被代理对象
     */
    private final Object target;

    public CoverageInvocationHandler(Object target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        ExecutionDataStore coverageBefore = JaCocoUtil.getCurrentCoverage();
        Object result = method.invoke(target, args);
        ExecutionDataStore coverageAfter = JaCocoUtil.getCurrentCoverage();
        double coverage = JaCocoUtil.analyzeCoverage(target.getClass(),coverageBefore, coverageAfter);
        return null;
    }
}
