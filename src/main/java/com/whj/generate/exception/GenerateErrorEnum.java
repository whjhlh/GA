package com.whj.generate.exception;

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

    ;


    private String code;
    private String desc;
    private String message;

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
