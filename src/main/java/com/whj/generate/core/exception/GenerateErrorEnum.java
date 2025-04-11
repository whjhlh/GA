package com.whj.generate.core.exception;

/**
 * @author whj
 * @date 2025-02-22 下午11:41
 */
public enum GenerateErrorEnum {
    /**
     * 未知异常
     */
    UNKNOWN_EXCEPTION("1000", "未知异常", "未知异常"),
    /**
     * 反射调用异常
     */
    REFLECTION_EXCEPTION("1001", "反射调用异常", "反射调用异常"),
    /**
     * 创建代理失败
     */
    CREATE_PROXY_FAIL("1002", "创建代理失败", "创建代理失败"),

    ;


    private String code;
    private String desc;
    private final String message;

     GenerateErrorEnum(String code, String desc, String message) {
        this.code = code;
        this.desc = desc;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
