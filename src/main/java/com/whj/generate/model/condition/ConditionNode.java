package com.whj.generate.model.condition;

import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.IfStmt;

import java.util.ArrayList;
import java.util.List;


/**
 * 条件树节点
 * @author whj
 * @date 2025-03-01 上午12:31
 */
public class ConditionNode {
    /**
     * 节点类型
     */
    NodeTypeEnum type;
    /**
     * 逻辑运算符
     */
    LogicalOperatorEnum logicalOp;
    /**
     * 比较运算符
     */
    ComparisonOperatorEnum comparisonOp;
    /**
     * 左操作数（变量名）
     */
    String leftOperand;
    /**
     * 右操作数（常量）
     */
    String rightOperand;
    /**
     * 子节点
     */
    List<ConditionNode> children;     // 子节点（当type=OPERATOR时有效）

    public ConditionNode(NodeTypeEnum type) {
        this.type = type;
        this.children = new ArrayList<>();
    }

    public static void printTree(ConditionNode root) {
        printNode(root, 0);
    }

    private static void printNode(ConditionNode node, int depth) {
        StringBuilder indent = new StringBuilder();
        for (int i = 0; i < depth; i++) indent.append("  ");

        if (node.type == NodeTypeEnum.OPERATOR) {
            System.out.println(indent + "└─ " + node.logicalOp);
            for (ConditionNode child : node.children) {
                printNode(child, depth + 1);
            }
        } else {
            String condition = String.format("%s %s %s",
                    node.leftOperand,
                    formatComparisonOp(node.comparisonOp),
                    node.rightOperand
            );
            System.out.println(indent + "└─ " + condition);
        }
    }

    private static String formatComparisonOp(ComparisonOperatorEnum op) {
        switch (op) {
            case EQUALS: return "==";
            case NOT_EQUALS: return "!=";
            case GREATER: return ">";
            case LESS: return "<";
            case GREATER_OR_EQUAL: return ">=";
            case LESS_OR_EQUAL: return "<=";
            default: return "?";
        }
    }

    public NodeTypeEnum getType() {
        return type;
    }

    public void setType(NodeTypeEnum type) {
        this.type = type;
    }

    public LogicalOperatorEnum getLogicalOp() {
        return logicalOp;
    }

    public void setLogicalOp(LogicalOperatorEnum logicalOp) {
        this.logicalOp = logicalOp;
    }

    public ComparisonOperatorEnum getComparisonOp() {
        return comparisonOp;
    }

    public void setComparisonOp(ComparisonOperatorEnum comparisonOp) {
        this.comparisonOp = comparisonOp;
    }

    public String getLeftOperand() {
        return leftOperand;
    }

    public void setLeftOperand(String leftOperand) {
        this.leftOperand = leftOperand;
    }

    public String getRightOperand() {
        return rightOperand;
    }

    public void setRightOperand(String rightOperand) {
        this.rightOperand = rightOperand;
    }

    public List<ConditionNode> getChildren() {
        return children;
    }

    public void setChildren(List<ConditionNode> children) {
        this.children = children;
    }

    /**
     * 解析单个if语句的条件表达式
     */
    public ConditionNode parseIfCondition(IfStmt ifStmt) {
        return parseExpression(ifStmt.getCondition());
    }

    /**
     * 递归解析表达式并构建条件树
     */
    private ConditionNode parseExpression(Expression expr) {
        if (expr instanceof BinaryExpr) {
            BinaryExpr binaryExpr = (BinaryExpr) expr;
            BinaryExpr.Operator op = binaryExpr.getOperator();

            // 处理逻辑运算符（AND/OR）
            if (op == BinaryExpr.Operator.AND || op == BinaryExpr.Operator.OR) {
                ConditionNode node = new ConditionNode(NodeTypeEnum.OPERATOR);
                node.logicalOp = (op == BinaryExpr.Operator.AND)
                        ? LogicalOperatorEnum.AND : LogicalOperatorEnum.OR;
                node.children.add(parseExpression(binaryExpr.getLeft()));
                node.children.add(parseExpression(binaryExpr.getRight()));
                return node;
            }
            // 处理比较运算符（如 >, == 等）
            else {
                return parseComparison(binaryExpr);
            }
        }
        // 处理其他复杂表达式（如括号、方法调用等）
        throw new UnsupportedOperationException("Unsupported expression: " + expr);
    }

    /**
     * 解析原子比较条件（如 a > 10）
     */
    private ConditionNode parseComparison(BinaryExpr expr) {
        ConditionNode node = new ConditionNode(NodeTypeEnum.CONDITION);
        node.leftOperand = expr.getLeft().toString();
        node.rightOperand = expr.getRight().toString();
        node.comparisonOp = parseComparisonOp(expr.getOperator());
        return node;
    }

    /**
     * 转换JavaParser运算符为自定义枚举
     */
    private ComparisonOperatorEnum parseComparisonOp(BinaryExpr.Operator op) {
        switch (op) {
            case EQUALS: return ComparisonOperatorEnum.EQUALS;
            case NOT_EQUALS: return ComparisonOperatorEnum.NOT_EQUALS;
            case GREATER: return ComparisonOperatorEnum.GREATER;
            case LESS: return ComparisonOperatorEnum.LESS;
            case GREATER_EQUALS: return ComparisonOperatorEnum.GREATER_OR_EQUAL;
            case LESS_EQUALS: return ComparisonOperatorEnum.LESS_OR_EQUAL;
            default: throw new IllegalArgumentException("Unsupported operator: " + op);
        }
    }
}
