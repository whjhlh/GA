package com.whj.generate.utill;

import com.whj.generate.handle.CoverageInvocationCGLIBHandler;
import com.whj.generate.handle.CoverageInvocationJDKHandler;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * @author whj
 * @date 2025-02-22 下午10:17
 */
public class ProxyUtil {
    /**
     * 创建覆盖动态代理<br/>
     * cglib动态代理<br>
     * 调用快
     *
     * @param targetClass 目标对象
     */
    public static Object createCoverageProxyCGLIB(Class<?> targetClass, MethodInterceptor handler) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(targetClass);
        enhancer.setCallback(handler);
        return enhancer.create();
    }
    /**
     * 创建覆盖动态代理<br/>
     * jdk动态代理<br/>
     * 生成快
     */
    public static Object createCoverageProxyJDK(Class<?> targetClass, InvocationHandler handler) {
        ClassLoader classLoader = targetClass.getClassLoader();
        Class[] interfaces = {targetClass};
        return Proxy.newProxyInstance(classLoader, interfaces, handler);
    }

    /**
     * 动态调用动态代理
     */
    public static Object createCoverageProxy(Class<?> clazz) {
        if (clazz == null) {
            return new Object();
        }
        //实现类接口
        if (ReflectionUtil.isImplementsInterface(clazz) && ReflectionUtil.isFinalClass(clazz) ) {
            return ProxyUtil.createCoverageProxyJDK(clazz, new CoverageInvocationJDKHandler());
        }
        if (ReflectionUtil.isFinalClass(clazz)) {
            return ProxyUtil.createCoverageProxyCGLIB(clazz, new CoverageInvocationCGLIBHandler());
        }
        throw new RuntimeException("不支持的类");
    }

}
