package com.whj.generate.condition;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.ast.stmt.IfStmt;
import com.whj.generate.utill.StringUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static com.whj.generate.utill.PathUtils.getFilePath;

public class ParamThresholdExtractor {

    public static Map<String, Set<Integer>> genGenetic(Class<?> clazz, String methodName) throws IOException {
        //根据反射获取code
        String code = getCode(clazz);
        CompilationUnit cu = StaticJavaParser.parse(code);
        Map<String, Set<Integer>> paramValues = new HashMap<>();
        cu.findAll(MethodDeclaration.class).forEach(method -> {
            if (!StringUtil.equals(method.getNameAsString(), methodName)) return;
            // 1. 获取参数列表
            List<String> params = method.getParameters().stream()
                    .map(NodeWithSimpleName::getNameAsString)
                    .toList();
            params.forEach(param -> paramValues.put(param, new HashSet<>()));
            // 2. 遍历所有if条件
            method.findAll(IfStmt.class).forEach(ifStmt -> {
                List<Expression> atomicConditions = new ArrayList<>();
                flattenConditions(ifStmt.getCondition(), atomicConditions);
                atomicConditions.forEach(expr -> extractThresholds(expr, params, paramValues));
            });

        });
        return paramValues;
    }

    /**
     * 获取代码
     * @param clazz
     * @return
     * @throws IOException
     */
    private static String getCode(Class<?> clazz) throws IOException {
        String filePath = getFilePath(clazz, StandardCharsets.UTF_8);
        return new String(Files.readAllBytes(Paths.get(filePath)));
    }


    // 递归分解复合条件表达式（支持 && 和 ||）
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

    // 提取临界值
    private static void extractThresholds(Expression expr, List<String> params, Map<String, Set<Integer>> paramValues) {
        if (!(expr instanceof BinaryExpr binaryExpr)) return;

        BinaryExpr.Operator op = binaryExpr.getOperator();
        switch (op) {
            case GREATER, LESS, EQUALS, GREATER_EQUALS, LESS_EQUALS -> {}
            default -> { return; }
        }


        // 处理左参数、右数值的情况（如 a > 1）
        // a(paramName) > 1(conditionValue)
        if (binaryExpr.getLeft() instanceof NameExpr && binaryExpr.getRight().isIntegerLiteralExpr()) {
            String paramName = ((NameExpr) binaryExpr.getLeft()).getNameAsString();
            if (params.contains(paramName)) {
                int conditionValue = binaryExpr.getRight().asIntegerLiteralExpr().asNumber().intValue();
                BinaryExpr.Operator reversedOp = reverseOperator(op);
                //加入条件为true
                addThresholds(paramName, op, conditionValue, paramValues);
                // 加入条件为false
                addThresholds(paramName, reversedOp, conditionValue, paramValues);
            }
        }

        // 处理右参数、左数值的情况（如 3 < a）
        // 3(conditionValue) < a(paramName)
        else if (binaryExpr.getRight() instanceof NameExpr && binaryExpr.getLeft().isIntegerLiteralExpr()) {
            String paramName = ((NameExpr) binaryExpr.getRight()).getNameAsString();
            if (params.contains(paramName)) {
                int value = binaryExpr.getLeft().asIntegerLiteralExpr().asNumber().intValue();
                BinaryExpr.Operator reversedOp = reverseOperator(op);
                addThresholds(paramName, reversedOp, value, paramValues);
            }
        }
    }

    // 反转操作符（例如将 > 转换为 <）
    private static BinaryExpr.Operator reverseOperator(BinaryExpr.Operator op) {
        return switch (op) {
            case GREATER -> BinaryExpr.Operator.LESS_EQUALS;
            case LESS -> BinaryExpr.Operator.GREATER_EQUALS;
            case GREATER_EQUALS -> BinaryExpr.Operator.LESS;
            case LESS_EQUALS -> BinaryExpr.Operator.GREATER;
            case EQUALS -> BinaryExpr.Operator.NOT_EQUALS;
            default -> op;
        };
    }

    // 根据操作符添加临界点
    private static void addThresholds(String paramName, BinaryExpr.Operator op, int conditionValue, Map<String, Set<Integer>> thresholds) {
        Set<Integer> integers = thresholds.get(paramName);
        switch (op) {
            case LESS:
                integers.add(conditionValue - 1);
                break;
            case GREATER, NOT_EQUALS:
                integers.add(conditionValue + 1);
                break;
            case LESS_EQUALS,GREATER_EQUALS,EQUALS:
                integers.add(conditionValue);
                break;
            default:
                break;
        }
    }
}
