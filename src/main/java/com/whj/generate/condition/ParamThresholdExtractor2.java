package com.whj.generate.condition;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.ast.stmt.IfStmt;
import com.whj.generate.utill.ReflectionUtil;

import java.io.IOException;
import java.util.*;

/**
 * @author whj
 * @date 2025-04-04 下午5:16
 */
public class ParamThresholdExtractor2 {
    private static final Map<BinaryExpr.Operator, BinaryExpr.Operator> REVERSE_OP_CACHE = new EnumMap<>(BinaryExpr.Operator.class);


    static {
        REVERSE_OP_CACHE.put(BinaryExpr.Operator.GREATER, BinaryExpr.Operator.LESS_EQUALS);
        REVERSE_OP_CACHE.put(BinaryExpr.Operator.LESS, BinaryExpr.Operator.GREATER_EQUALS);
        REVERSE_OP_CACHE.put(BinaryExpr.Operator.GREATER_EQUALS, BinaryExpr.Operator.LESS);
        REVERSE_OP_CACHE.put(BinaryExpr.Operator.LESS_EQUALS, BinaryExpr.Operator.GREATER);
        REVERSE_OP_CACHE.put(BinaryExpr.Operator.EQUALS, BinaryExpr.Operator.NOT_EQUALS);
    }

    public static Map<String, Set<Object>> genGenetic(Class<?> clazz, String methodName) throws IOException {
        String code = ReflectionUtil.getCode(clazz);
        CompilationUnit cu = StaticJavaParser.parse(code);
        Map<String, Set<Object>> paramValues = new HashMap<>();

        cu.getClassByName(clazz.getSimpleName()).ifPresent(classDecl -> {
            classDecl.getMethodsByName(methodName).forEach(method -> {
                List<String> params = method.getParameters().stream()
                        .map(NodeWithSimpleName::getNameAsString)
                        .toList();
                params.forEach(param -> paramValues.put(param, new TreeSet<>()));
                method.findAll(IfStmt.class).forEach(ifStmt -> {
                    List<Expression> atomicConditions = new ArrayList<>();
                    flattenConditions(ifStmt.getCondition(), atomicConditions);
                    atomicConditions.forEach(expr -> extractThresholds(expr, params, paramValues));
                });
            });
        });
        return paramValues;
    }
    /**
     * 扁平化条件
     * @param expression
     * @param atomicConditions
     */
    private static void flattenConditions(Expression expression, List<Expression> atomicConditions) {
        if (expression instanceof BinaryExpr binaryExpr) {
            BinaryExpr.Operator op = binaryExpr.getOperator();
            if (op == BinaryExpr.Operator.AND || op == BinaryExpr.Operator.OR) {
                flattenConditions(binaryExpr.getLeft(), atomicConditions);
                flattenConditions(binaryExpr.getRight(), atomicConditions);
                return;
            }
        }
        atomicConditions.add(expression);
    }

    private static void extractThresholds(Expression expr, List<String> params, Map<String, Set<Object>> paramValues) {
        if (!(expr instanceof BinaryExpr binaryExpr)) return;
        BinaryExpr.Operator op = binaryExpr.getOperator();
        BinaryExpr.Operator reversedOp = REVERSE_OP_CACHE.getOrDefault(op, op);
        // 仅处理指定操作符
        if (checkOp(op)) return;

        // 处理左参数、右数值的情况
        Expression right = binaryExpr.getRight();
        Expression left = binaryExpr.getLeft();
        //整型处理
        //todo 枚举类型处理
        //处理正向逻辑和逆向逻辑
        handledThresholdsByInt(params, paramValues, left, right, op);
        handledThresholdsByInt(params, paramValues, right, left, reversedOp);
        handledThresholdsByEnumCode(params, paramValues, left, right, op);
        handledThresholdsByEnumCode(params, paramValues, right, left, reversedOp);
    }

    /**
     * 处理枚举类型
     * @param params
     * @param paramValues
     * @param left
     * @param right
     * @param op
     */
    private static void handledThresholdsByEnumCode(List<String> params, Map<String, Set<Object>> paramValues, Expression left, Expression right, BinaryExpr.Operator op) {
        if (left instanceof NameExpr leftName && right.isStringLiteralExpr()) {
            String paramName = leftName.getNameAsString();
            if (params.contains(paramName)) {
                String value = right.asStringLiteralExpr().getValue();
                addThresholdsByString(paramName, op, value, paramValues);
            }
        }
    }

    private static void handledThresholdsByInt(List<String> params, Map<String, Set<Object>> paramValues, Expression left, Expression right, BinaryExpr.Operator op) {
        if (left instanceof NameExpr leftName && right.isIntegerLiteralExpr()) {
            String paramName = leftName.getNameAsString();
            if (params.contains(paramName)) {
                int value = right.asIntegerLiteralExpr().asNumber().intValue();
                addThresholdsByInt(paramName, op, value, paramValues);
            }
        }
    }
    private static void addThresholdsByString(String paramName, BinaryExpr.Operator op, String value, Map<String, Set<Object>> thresholds) {
        Set<Object> values = thresholds.get(paramName);
        if (op == BinaryExpr.Operator.EQUALS || op == BinaryExpr.Operator.NOT_EQUALS) {
            values.add(value);
        }
    }

    /**
     * 检查操作符
     * @param op
     * @return
     */
    private static boolean checkOp(BinaryExpr.Operator op) {
        switch (op) {
            case GREATER:
            case LESS:
            case EQUALS:
            case GREATER_EQUALS:
            case LESS_EQUALS:
                break;
            default:
                return true;
        }
        return false;
    }

    /**
     * 生成正向和反向结果值
     * @param paramName
     * @param op
     * @param value
     * @param thresholds
     */
    private static void addThresholdsByInt(String paramName, BinaryExpr.Operator op, int value, Map<String, Set<Object>> thresholds) {
        Set<Object> values = thresholds.get(paramName);
        switch (op) {
            case EQUALS ->{
                values.add(value - 1);
                values.add(value);
                values.add(value + 1);
            }
            case LESS -> {
                values.add(value - 1);
                values.add(value);
            }
            case GREATER -> {
                values.add(value);
                values.add(value + 1);
            }
            case LESS_EQUALS, GREATER_EQUALS -> values.add(value);
            case NOT_EQUALS -> {
                values.add(value - 1);
                values.add(value);
                values.add(value + 1);
            }
            default -> {}
        }
    }
}
