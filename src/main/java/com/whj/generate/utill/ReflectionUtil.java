package com.whj.generate.utill;

import com.whj.generate.exception.ExceptionWrapper;
import com.whj.generate.exception.GenerateErrorEnum;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.Callable;

/**
 * @author whj
 * @date 2025-02-22 下午11:31
 */
public class ReflectionUtil {
    /**
     * 反射调用方法
     *
     * @param obj
     * @param method
     * @param args
     */
    public static Object invokeMethod(final Object obj, final Method method, Object... args) {
        if (null == method) {
            return null;
        }
        return ExceptionWrapper.process(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                return method.invoke(obj, args);
            }
        }, GenerateErrorEnum.REFLECTION_EXCEPTION, ",%s反射调用%s方法异常[%s]", obj.getClass().getName(), method.getName(), args);
    }

    /**
     * 判断是否是final方法
     *
     * @param method
     */
    public static boolean isFinalMethod(Method method) {
        if(null == method){
            return false;
        }
        return Modifier.isFinal(method.getModifiers());
    }

    /**
     * 判断是否是final类
     *
     * @param clazz
     */
    public static boolean isFinalClass(Class<?> clazz) {
        if(null == clazz){
            return false;
        }
        return Modifier.isFinal(clazz.getModifiers());
    }
    /**
     * 判断一个类是否实现一个或多个接口
     */
    public static boolean isImplementsInterface(Class<?> clazz) {
        if(null == clazz){
            return false;
        }
        return clazz.getInterfaces().length > 0;
    }
}
