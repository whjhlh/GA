package com.whj.generate.core.bizenum;

/**
 * 逻辑运算符类型
 * @author whj
 * @date 2025-03-01 上午12:31
 */
public enum LogicalOperatorEnum {
    /**
     * 或操作符
     * 表示逻辑或运算
     */
    OR("or", "||", "或操作符"),
    /**
     * 且操作符
     * 表示逻辑与运算
     */
    AND("and", "&&", "且操作符"),
    /**
     * 二进制或操作符
     * 表示按位或运算
     */
    BINARY_OR("binary_or", "|", "二进制或操作符"),
    /**
     * 二进制且操作符
     * 表示按位与运算
     */
    BINARY_AND("binary_and", "&", "二进制且操作符"),
    /**
     * 异或操作符
     * 表示按位异或运算
     */
    XOR("xor", "^", "异或操作符"),
    /**
     * 等于操作符
     * 表示两个值相等
     */
    EQUALS("equals", "==", "等于操作符"),
    /**
     * 不等于操作符
     * 表示两个值不相等
     */
    NOT_EQUALS("not_equals", "!=", "不等于操作符"),
    /**
     * 小于操作符
     * 表示左侧的值小于右侧的值
     */
    LESS("less", "<", "小于操作符"),
    /**
     * 大于操作符
     * 表示左侧的值大于右侧的值
     */
    GREATER("greater", ">", "大于操作符"),
    /**
     * 小于等于操作符
     * 表示左侧的值小于或等于右侧的值
     */
    LESS_EQUALS("less_equals", "<=", "小于等于操作符"),
    /**
     * 大于等于操作符
     * 表示左侧的值大于或等于右侧的值
     */
    GREATER_EQUALS("greater_equals", ">=", "大于等于操作符"),
    /**
     * 左移操作符
     * 表示将左侧操作数的二进制表示向左移动指定的位数
     */
    LEFT_SHIFT("left_shift", "<<", "左移操作符"),
    /**
     * 有符号右移操作符
     * 表示将左侧操作数的二进制表示向右移动指定的位数，左侧用符号位填充
     */
    SIGNED_RIGHT_SHIFT("signed_right_shift", ">>", "有符号右移操作符"),
    /**
     * 无符号右移操作符
     * 表示将左侧操作数的二进制表示向右移动指定的位数，左侧用零填充
     */
    UNSIGNED_RIGHT_SHIFT("unsigned_right_shift", ">>>", "无符号右移操作符"),
    /**
     * 加法操作符
     * 表示将两个值相加
     */
    PLUS("plus", "+", "加法操作符"),
    /**
     * 减法操作符
     * 表示将右侧的值从左侧的值中减去
     */
    MINUS("minus", "-", "减法操作符"),
    /**
     * 乘法操作符
     * 表示将两个值相乘
     */
    MULTIPLY("multiply", "*", "乘法操作符"),
    /**
     * 除法操作符
     * 表示将左侧的值除以右侧的值
     */
    DIVIDE("divide", "/", "除法操作符"),
    /**
     * 取余操作符
     * 表示左侧的值除以右侧的值后的余数
     */
    REMAINDER("remainder", "%", "取余操作符");

    /**
     * code
     */
    private final String code;
    /**
     * 运算符
     */
    private final String op;
    /**
     * 描述
     */
    private final String desc;

    LogicalOperatorEnum(String code, String op, String desc) {
        this.code = code;
        this.op = op;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getOp() {
        return op;
    }

    public String getDesc() {
        return desc;
    }
}
