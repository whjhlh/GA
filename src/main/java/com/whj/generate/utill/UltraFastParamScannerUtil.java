package com.whj.generate.utill;

/**
 * @author whj
 * @date 2025-04-07 下午9:52
 */

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UltraFastParamScannerUtil {

    // 匹配方法签名及参数列表的正则（优化版）
    private static final Pattern METHOD_PATTERN = Pattern.compile(
            "\\s*public\\s+void\\s+" + // 方法修饰符+返回类型+方法名
                    "(\\w+)\\s*\\(([^)]*)\\)"  // 方法名和参数列表
    );

    // 匹配单个参数的正则
    private static final Pattern PARAM_PATTERN = Pattern.compile(
            "(\\w+)\\s+(\\w+)(?:\\s*,\\s*|$)" // 类型+参数名
    );

    /**
     * 基于正则的极速解析（比JavaParser快5倍）
     */
    public static Map<String, String> scanParamsUltraFast(String code, String targetMethod) throws IOException {
        Matcher methodMatcher = METHOD_PATTERN.matcher(code);
        Map<String, String> paramMap = new LinkedHashMap<>();

        while (methodMatcher.find()) {
            if (methodMatcher.group(1).equals(targetMethod)) {
                String paramsStr = methodMatcher.group(2);
                Matcher paramMatcher = PARAM_PATTERN.matcher(paramsStr);
                while (paramMatcher.find()) {
                    paramMap.put(
                            paramMatcher.group(2), // 参数名
                            paramMatcher.group(1)  // 类型简名
                    );
                }
                break; // 找到目标方法后立即退出
            }
        }
        return paramMap;
    }

}
