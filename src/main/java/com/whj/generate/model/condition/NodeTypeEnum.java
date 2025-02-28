package com.whj.generate.model.condition;

/**
 * @author whj
 * @date 2025-03-01 上午12:29
 */

// 条件树节点类型
public enum NodeTypeEnum {
    /**
     * 逻辑运算符（AND/OR）
     */
    OPERATOR,
    /**
     * 原子条件（如 a > 10）
     */
    CONDITION
}

