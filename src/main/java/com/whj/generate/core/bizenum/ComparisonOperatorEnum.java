package com.whj.generate.core.bizenum;

/**
 * @author whj
 * @date 2025-03-01 上午12:31
 */ // 比较运算符类型
public enum ComparisonOperatorEnum {
    EQUALS("equals","等于"),
    NOT_EQUALS("notEquals","不等于"),
    GREATER("greater","大于"),
    LESS("less","小于"),
    GREATER_OR_EQUAL("greaterOrEqual","大于等于"),
    LESS_OR_EQUAL("lessOrEqual","小于等于");

    private final String code;
    private final String desc;

    ComparisonOperatorEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
