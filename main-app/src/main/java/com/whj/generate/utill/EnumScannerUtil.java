package com.whj.generate.utill;


import org.objectweb.asm.*;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 枚举扫描工具类（增强版）
 * 1. 扫描方法体中使用的枚举值（GETSTATIC指令）
 * 2. 扫描方法参数的枚举类型
 * 3. 合并结果到统一的enumMap
 */
public class EnumScannerUtil {
    private static final Map<String,Map<String, List<String>>> ENUM_CACHE= new ConcurrentHashMap<>();

    private static final Map<String, Class<?>> CLASS_CACHE = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Method> METHOD_CACHE = new ConcurrentHashMap<>();

    private static void scanParameterEnums(String methodDesc, Map<String, List<String>> enumMap) {
        Arrays.stream(Type.getArgumentTypes(methodDesc))
                .parallel()
                .filter(t -> t.getDescriptor().startsWith("L") && t.getDescriptor().endsWith(";"))
                .map(t -> loadClassSafely(t.getClassName()))
                .filter(cls -> cls != Void.class && cls.isEnum())
                .forEach(cls -> cacheEnumCodes(cls, enumMap));
    }
    private static Method getCodeMethod(Class<?> enumClass) {
        return METHOD_CACHE.computeIfAbsent(enumClass, cls -> {
            try {
                return cls.getMethod("getCode");
            } catch (NoSuchMethodException e) {
                return null;
            }
        });
    }
    private static Class<?> loadClassSafely(String className) {
        return CLASS_CACHE.computeIfAbsent(className, cn -> {
            try {
                return Class.forName(cn);
            } catch (ClassNotFoundException e) {
                return Void.class; // 标记无效类型
            }
        });
    }
    /**
     * 扫描方法体和参数中的枚举
     * @param clazz      目标类
     * @param methodName 方法名
     * @return Map<枚举类名, List<code>>
     */
    public static Map<String, List<String>> findEnumsInMethod(Class<?> clazz, String methodName) throws IOException {
        String cacheKey = clazz.getName() + "#" + methodName;
        return ENUM_CACHE.computeIfAbsent(cacheKey, key -> {
            try {
                Map<String, List<String>> res = new HashMap<>();
                scanClass(clazz, methodName, res);
                return Collections.unmodifiableMap(res);
            }catch (Exception e){
                return  Collections.emptyMap();
            }
        });
    }

    private static void scanClass(Class<?> clazz, String methodName, Map<String, List<String>> res) throws IOException {
        String className = clazz.getName().replace('.', '/');
        String resourcePath = "/" + className + ".class";

        try (InputStream is = clazz.getResourceAsStream(resourcePath)) {
            if (is == null) return;

            new ClassReader(is).accept(new ClassVisitor(Opcodes.ASM9) {
                @Override
                public MethodVisitor visitMethod(int access, String name, String descriptor,
                                                 String signature, String[] exceptions) {
                    if (name.equals(methodName)) {
                        // 1. 先扫描方法参数的枚举类型
                        scanParameterEnums(descriptor, res);
                        // 2. 再扫描方法体中的枚举使用
                        return new EnumMethodVisitor(res);
                    }
                    return null;
                }
            }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        }
    }

    // 缓存枚举的code值
    private static void cacheEnumCodes(Class<?> enumClass, Map<String, List<String>> enumMap) {
        String enumName = enumClass.getSimpleName();
        if (enumMap.containsKey(enumName)) return; // 避免重复处理

        try {
            List<String> codes = new ArrayList<>();
            Method getCode = enumClass.getMethod("getCode");
            for (Object constant : enumClass.getEnumConstants()) {
                String string = getCode.invoke(constant).toString();
                codes.add(string.toLowerCase());
            }
            enumMap.put(enumName, codes);
        } catch (Exception e) {
            System.err.println("Failed to get codes from enum: " + enumName);
        }
    }

    // 方法体枚举扫描器
    private static class EnumMethodVisitor extends MethodVisitor {
        private final Map<String, List<String>> enumMap;

        EnumMethodVisitor(Map<String, List<String>> enumMap) {
            super(Opcodes.ASM9);
            this.enumMap = enumMap;
        }

        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String desc) {
            // 只处理GETSTATIC和枚举类型
            if (opcode != Opcodes.GETSTATIC || !desc.startsWith("L") || !desc.endsWith(";")) {
                return;
            }

            // 如果已处理过该枚举则跳过
            String enumClassName = desc.substring(1, desc.length() - 1).replace('/', '.');
            if (enumMap.containsKey(enumClassName.substring(enumClassName.lastIndexOf('.') + 1))) {
                return;
            }

            try {
                Class<?> enumClass = Class.forName(enumClassName);
                if (enumClass.isEnum()) {
                    cacheEnumCodes(enumClass, enumMap);
                }
            } catch (Exception e) {
                // 忽略异常
            }
        }
    }
}