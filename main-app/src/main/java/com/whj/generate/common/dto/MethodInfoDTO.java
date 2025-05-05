package com.whj.generate.common.dto;

/**
 * @author whj
 * @date 2025-05-05 下午10:55
 */

public class MethodInfoDTO {
    /**
     * 方法名
     */
    private final String methodName;
    /**
     * 返回值类型
     */
    private final String returnType;

    public MethodInfoDTO(String methodName, String returnType) {
        this.methodName = methodName;
        this.returnType = returnType;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getReturnType() {
        return returnType;
    }
}
