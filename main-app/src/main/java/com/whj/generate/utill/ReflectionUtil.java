package com.whj.generate.utill;

import com.whj.generate.core.exception.ExceptionWrapper;
import com.whj.generate.core.exception.GenerateErrorEnum;
import org.springframework.util.ClassUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

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
     * 调用方法
     *
     * @param method
     * @param params
     * @param instance
     * @return
     */
    public static String invokeSafe(Method method, Object[] params, Object instance) {
        try {
            // 参数数量检查
            Class<?>[] paramTypes = method.getParameterTypes();
            if (params.length != paramTypes.length) {
                return String.format("参数数量不匹配，预期: %d，实际: %d", paramTypes.length, params.length);
            }

            // 参数类型转换
            Object[] convertedParams = new Object[params.length];
            for (int i = 0; i < params.length; i++) {
                Object param = params[i];
                Class<?> targetType = paramTypes[i];

                if (param == null) {
                    convertedParams[i] = null;
                } else if (targetType == int.class || targetType == Integer.class) {
                    convertedParams[i] = Integer.parseInt(param.toString());
                } else if (targetType.isEnum()) {
                    String enumValue = param.toString().toUpperCase();
                    convertedParams[i] = Enum.valueOf(targetType.asSubclass(Enum.class), enumValue);
                } else {
                    convertedParams[i] = param;
                }
            }

            // 设置私有方法可访问
            if (!method.isAccessible()) {
                method.setAccessible(true);
            }

            method.invoke(instance, convertedParams);
            return null; // 成功返回 null
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            return String.format("%s", cause != null ? cause.getMessage() : e.getMessage());
        } catch (IllegalArgumentException e) {
            return String.format("参数类型错误：%s", e.getMessage());
        } catch (Exception e) {
            return String.format("反射调用异常：%s", e.getMessage());
        }
    }


    /**
     * 判断是否是final方法
     *
     * @param method
     */
    public static boolean isFinalMethod(Method method) {
        if (null == method) {
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
        if (null == clazz) {
            return false;
        }
        return Modifier.isFinal(clazz.getModifiers());
    }

    /**
     * 判断一个类是否实现一个或多个接口
     */
    public static boolean isImplementsInterface(Class<?> clazz) {
        if (null == clazz) {
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
     *
     * @param clazz
     * @param filePath
     * @return
     * @throws IOException
     */
    public static String getJavaCode(Class<?> clazz, String filePath) throws IOException {

        try (InputStream is = Files.newInputStream(Paths.get(filePath))) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    public static Set<Class<?>> findClassesInPackage(String packageName) throws IOException {
        Set<Class<?>> classes = new HashSet<>();
        String path = packageName.replace('.', '/');
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> resources = classLoader.getResources(path);

        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            if (resource.getProtocol().equals("file")) {
                File directory = new File(resource.getFile());
                scanDirectory(packageName, directory, classes);
            } else if (resource.getProtocol().equals("jar")) {
                scanJar(resource, packageName, classes);
            }
        }
        return classes;
    }

    private static void scanDirectory(String packageName, File directory, Set<Class<?>> classes) {
        File[] files = directory.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                scanDirectory(packageName + "." + file.getName(), file, classes);
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + '.' + file.getName().replace(".class", "");
                loadClass(className, classes);
            }
        }
    }

    private static void scanJar(URL jarUrl, String packageName, Set<Class<?>> classes) {
        try (JarFile jarFile = ((JarURLConnection) jarUrl.openConnection()).getJarFile()) {
            Enumeration<JarEntry> entries = jarFile.entries();
            String packagePath = packageName.replace('.', '/');

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (name.startsWith(packagePath) && name.endsWith(".class")) {
                    String className = name.replace("/", ".").replace(".class", "");
                    loadClass(className, classes);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取class类种方法名为methodName的方法
     *
     * @param methodName
     * @param clazz
     */
    public static Method findMethod(Class<?> clazz, String methodName) {
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        return null;
    }

    private static void loadClass(String className, Set<Class<?>> classes) {
        try {
            Class<?> clazz = Class.forName(className);
            if (clazz.isEnum()) {
                classes.add(clazz);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
