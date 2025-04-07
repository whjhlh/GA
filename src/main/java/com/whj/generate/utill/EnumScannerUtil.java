package com.whj.generate.utill;

import org.objectweb.asm.*;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 枚举扫描工具类（增强版）
 * 1. 扫描方法体中使用的枚举值（GETSTATIC指令）
 * 2. 扫描方法参数的枚举类型
 * 3. 合并结果到统一的enumMap
 */
public class EnumScannerUtil {

    /**
     * 扫描方法体和参数中的枚举
     * @param clazz      目标类
     * @param methodName 方法名
     * @return Map<枚举类名, List<code>>
     */
    public static Map<String, List<String>> findEnumsInMethod(Class<?> clazz, String methodName) throws IOException {
        Map<String, List<String>> enumMap = new HashMap<>();
        String className = clazz.getName().replace('.', '/');
        String resourcePath = "/" + className + ".class";

        try (InputStream is = clazz.getResourceAsStream(resourcePath)) {
            if (is == null) throw new IOException("Class file not found: " + resourcePath);

            ClassReader reader = new ClassReader(is);
            reader.accept(new ClassVisitor(Opcodes.ASM9) {
                @Override
                public MethodVisitor visitMethod(int access, String name, String descriptor,
                                                 String signature, String[] exceptions) {
                    if (name.equals(methodName)) {
                        // 1. 先扫描方法参数的枚举类型
                        scanParameterEnums(descriptor, enumMap);
                        // 2. 再扫描方法体中的枚举使用
                        return new EnumMethodVisitor(enumMap);
                    }
                    return null;
                }
            }, ClassReader.SKIP_DEBUG);
        }
        return enumMap;
    }

    // 扫描方法参数的枚举类型
    private static void scanParameterEnums(String methodDesc, Map<String, List<String>> enumMap) {
        Type[] argTypes = Type.getArgumentTypes(methodDesc);
        for (Type argType : argTypes) {
            if (argType.getDescriptor().startsWith("L") && argType.getDescriptor().endsWith(";")) {
                String enumClassName = argType.getClassName();
                try {
                    Class<?> enumClass = Class.forName(enumClassName);
                    if (enumClass.isEnum()) {
                        cacheEnumCodes(enumClass, enumMap);
                    }
                } catch (Exception e) {
                    // 忽略加载失败的类
                }
            }
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
            // 捕获GETSTATIC枚举指令
            if (opcode == Opcodes.GETSTATIC && desc.startsWith("L") && desc.endsWith(";")) {
                String enumClassName = desc.substring(1, desc.length() - 1).replace('/', '.');
                try {
                    Class<?> enumClass = Class.forName(enumClassName);
                    if (enumClass.isEnum()) {
                        cacheEnumCodes(enumClass, enumMap);
                    }
                } catch (Exception e) {
                    // 忽略加载失败的类
                }
            }
        }
    }
}