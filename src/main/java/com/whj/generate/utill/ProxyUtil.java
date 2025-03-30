package com.whj.generate.utill;

import com.whj.generate.exception.ExceptionWrapper;
import com.whj.generate.exception.GenerateErrorEnum;
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

     * @param
     */
    public static Object createCoverageProxyCGLIB(Object target, MethodInterceptor handler) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(target.getClass());
        enhancer.setCallback(handler);
        enhancer.setUseFactory(false);
        return enhancer.create(new Class<?>[0], new Object[0]);
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
    /**
     * 创建动态代理（基于目标实例）
     */
    public static Object createCoverageProxy(Object target) {
        return ExceptionWrapper.process(() -> {
            if (target == null) {
                throw new IllegalArgumentException("Target object cannot be null");
            }
            Class<?> clazz = target.getClass();
            if (clazz.isInterface()) {
                return createCoverageProxyJDK(target);
            } else {
                return createCoverageProxyCGLIB(target, new CoverageInvocationCGLIBHandler(target));
            }
        }, GenerateErrorEnum.CREATE_PROXY_FAIL, "创建动态代理失败: %s", target.getClass().getName());

    }
    /**
     * 创建 JDK 代理（需接口）
     */
    private static Object createCoverageProxyJDK(Object target) {
        Class<?>[] interfaces = target.getClass().getInterfaces();
        if (interfaces.length == 0) {
            throw new IllegalArgumentException("JDK 代理需要目标类实现接口");
        }
        return Proxy.newProxyInstance(
                target.getClass().getClassLoader(),
                interfaces,
                new CoverageInvocationJDKHandler(target) // 传入目标实例
        );
    }

}
