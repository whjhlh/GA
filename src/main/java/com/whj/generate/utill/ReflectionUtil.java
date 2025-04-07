package com.whj.generate.utill;

import com.whj.generate.exception.ExceptionWrapper;
import com.whj.generate.exception.GenerateErrorEnum;
import org.springframework.util.ClassUtils;

import java.io.File;
import java.io.IOException;
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
