package com.whj.generate.utill;

/**
 * @author whj
 * @date 2025-04-07 下午9:52
 */

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UltraFastParamScannerUtil {

    // 使用static final
    private static final Pattern METHOD_PATTERN = Pattern.compile(
            "\\s*public\\s+void\\s+(\\w+)\\s*\\(([^)]*)\\)",
            Pattern.DOTALL); // 添加DOTALL标志支持多行方法

    private static final Pattern PARAM_PATTERN = Pattern.compile(
            "(\\w+)\\s+(\\w+)(?:\\s*,\\s*|$)",
            Pattern.DOTALL);

    /**
     * 基于正则的极速解析
     */
    public static Map<String, String> scanParamsUltraFast(String code, String targetMethod) {
        int methodStart = code.indexOf("public void " + targetMethod + "(");
        if (methodStart == -1) return Collections.emptyMap();

        int paramsEnd = code.indexOf(')', methodStart);
        Map<String, String> paramMap = new LinkedHashMap<>();
        // 使用区域匹配提升性能
        Matcher methodMatcher = METHOD_PATTERN.matcher(code);
        while (methodMatcher.find()) {
            if (targetMethod.equals(methodMatcher.group(1))) {
                String paramsStr = methodMatcher.group(2);
                // 简单分割参数
                String[] params = paramsStr.split(",");
                for (String param : params) {
                    int lastSpace = param.trim().lastIndexOf(' ');
                    if (lastSpace != -1) {
                        paramMap.put(
                                param.substring(lastSpace+1).trim(),
                                param.substring(0, lastSpace).trim()
                        );
                    }
                }
                break;
            }
        }
        return paramMap;
    }

}
