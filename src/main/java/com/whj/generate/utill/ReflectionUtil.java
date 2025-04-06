package com.whj.generate.utill;

import com.whj.generate.exception.ExceptionWrapper;
import com.whj.generate.exception.GenerateErrorEnum;
import org.springframework.util.ClassUtils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

import static com.whj.generate.utill.PathUtils.getFilePath;

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
        }, GenerateErrorEnum.REFLECTION_EXCEPTION, ",%s反射调用%s方法异常%s", ClassUtils.getShortName(obj.getClass()), method.getName(), JsonUtil.toJson(args));
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


    /**
     * 获取原始目标类（处理 CGLIB 代理）
     */
    public static Class<?> getTargetClass(Object proxyObj) {
        Class<?> clazz = proxyObj.getClass();
        while (isCglibProxyClass(clazz)) {
            clazz = clazz.getSuperclass(); // 获取原始类
        }
        return clazz;
    }

    /**
     * 判断是否为 CGLIB 代理类
     */
    private static boolean isCglibProxyClass(Class<?> clazz) {
        return clazz.getName().contains("$$EnhancerByCGLIB$$");
    }

    /**
     * 获取代码
     * @param clazz
     * @return
     * @throws IOException
     */
    public static String getCode(Class<?> clazz) throws IOException {
        String filePath = getFilePath(clazz, StandardCharsets.UTF_8);
        return new String(Files.readAllBytes(Paths.get(filePath)));
    }
}
