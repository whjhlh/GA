package com.whj.generate.utill;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author whj
 * @date 2025-03-30 下午9:35
 */
public class PathUtils {
    private PathUtils() {} // 防止实例化

    static String toClassResourcePath(Class<?> clazz) {
        return clazz.getName().replace('.', '/') + ".class";
    }

    static String getClassFilePath(Class<?> clazz) throws IOException {
        final String resourcePath = toClassResourcePath(clazz);
        final URL url = clazz.getClassLoader().getResource(resourcePath);
        if (url == null) {
            throw new FileNotFoundException("Class resource not found: " + resourcePath);
        }
        return URLDecoder.decode(url.getFile(), StandardCharsets.UTF_8);
    }
    /**
     * 获取类源码路径
     * @param clazz
     * @param charset
     * @return
     */
    public static String getFilePath(Class<?> clazz, Charset charset) {
        try {
            // 修正资源路径格式
            String classResource = "/" + clazz.getName().replace(".", "/") + ".class";
            URL classURL = clazz.getResource(classResource);
            if (classURL == null) {
                throw new RuntimeException("类资源未找到: " + classResource);
            }
            // 禁止在 JAR 中运行
            if ("jar".equals(classURL.getProtocol())) {
                throw new RuntimeException("不支持从 JAR 包中获取源码路径");
            }
            // 解码路径并替换为源码目录
            String classFilePath = URLDecoder.decode(classURL.getPath(), charset);
            String sourceRoot = classFilePath
                    .replaceAll(Pattern.quote(File.separator + "target" + File.separator + "classes"),
                            Matcher.quoteReplacement(File.separator + "src" + File.separator + "main" + File.separator + "java"));
            // 移除尾部的 .class 并添加 .java
            if (sourceRoot.endsWith(".class")) {
                sourceRoot = sourceRoot.substring(0, sourceRoot.length() - 6) + ".java";
            }
            return sourceRoot;
        } catch (Exception e) {
            throw new RuntimeException("获取源码路径失败", e);
        }
    }
}
