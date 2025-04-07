package com.whj.generate.condition;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.ast.stmt.IfStmt;
import com.whj.generate.dualPipeline.testForCover;
import com.whj.generate.utill.ReflectionUtil;
import com.whj.generate.utill.StringUtil;

import java.io.IOException;
import java.util.*;

import static com.whj.generate.utill.EnumScannerUtil.findEnumsInMethod;
import static com.whj.generate.utill.UltraFastParamScannerUtil.scanParamsUltraFast;

/**
 * @author whj
 * @date 2025-04-04 下午5:16
 */
public class ParamThresholdExtractor2 {
    private static final Map<BinaryExpr.Operator, BinaryExpr.Operator> REVERSE_OP_CACHE = new EnumMap<>(BinaryExpr.Operator.class);
    private static final Map<String, List<String>> ENUM_CODE_MAP;
    private static  Map<String, String> PARAM_NAME_SIMPLE_NAME_MAP;

    static {
        REVERSE_OP_CACHE.put(BinaryExpr.Operator.GREATER, BinaryExpr.Operator.LESS_EQUALS);
        REVERSE_OP_CACHE.put(BinaryExpr.Operator.LESS, BinaryExpr.Operator.GREATER_EQUALS);
        REVERSE_OP_CACHE.put(BinaryExpr.Operator.GREATER_EQUALS, BinaryExpr.Operator.LESS);
        REVERSE_OP_CACHE.put(BinaryExpr.Operator.LESS_EQUALS, BinaryExpr.Operator.GREATER);
        REVERSE_OP_CACHE.put(BinaryExpr.Operator.EQUALS, BinaryExpr.Operator.NOT_EQUALS);
        try {
            ENUM_CODE_MAP = findEnumsInMethod(testForCover.class,"test");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, Set<Object>> genGenetic(Class<?> clazz, String methodName) throws IOException {
        String code = ReflectionUtil.getCode(clazz);
        PARAM_NAME_SIMPLE_NAME_MAP = scanParamsUltraFast(code, "test");
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
        if (expr instanceof BinaryExpr binaryExpr) {
            BinaryExpr.Operator op = binaryExpr.getOperator();
            BinaryExpr.Operator reversedOp = REVERSE_OP_CACHE.getOrDefault(op, op);
            // 仅处理指定操作符
            if (checkOp(op)) return;

            // 处理左参数、右数值的情况
            Expression right = binaryExpr.getRight();
            Expression left = binaryExpr.getLeft();
            //【1】整型处理 【2】枚举 【3】字符串
            //处理正向逻辑和逆向逻辑
            handledThresholdsByInt(params, paramValues, left, right, op);
            handledThresholdsByInt(params, paramValues, right, left, reversedOp);
            handledThresholdsByEnumCode(params, paramValues, left, right, op);
            handledThresholdsByEnumCode(params, paramValues, right, left, reversedOp);
        }else if(expr instanceof MethodCallExpr methodCall){
            handleEnumMethodCall(methodCall, params, paramValues);
        }

    }
    private static void handleEnumMethodCall(MethodCallExpr methodCall, List<String> params, Map<String, Set<Object>> paramValues) {
        // 仅处理 equals 方法调用
        if (!methodCall.getNameAsString().equals("equals")) {
            return;
        }

        // 检查是否为 enmu.getCode().equals("equals") 结构
        if (methodCall.getScope().isPresent() &&
                methodCall.getScope().get() instanceof MethodCallExpr scopeCall) {

            // 确认是 getCode() 方法调用
            if (scopeCall.getNameAsString().equals("getCode") &&
                    scopeCall.getScope().isPresent() &&
                    scopeCall.getScope().get() instanceof NameExpr enumNameExpr) {

                String paramName = enumNameExpr.getNameAsString();
                if (params.contains(paramName)) {
                    // 获取 equals 的参数值（如 "equals"）
                    if (methodCall.getArgument(0).isStringLiteralExpr()) {
                        String comparedValue = methodCall.getArgument(0).asStringLiteralExpr().getValue();
                        // 添加枚举的 code 值到结果集
                        addEnumCodeThresholds(paramName, comparedValue, paramValues);
                    }
                }
            }
        }
    }
    private static void addEnumCodeThresholds(String paramName, String comparedValue, Map<String, Set<Object>> paramValues) {
        Set<Object> values = paramValues.computeIfAbsent(paramName, k -> new TreeSet<>());

        // 从预加载的 ENUM_CODE_MAP 中获取该枚举的所有 code 值
        String clazzSimpleName = PARAM_NAME_SIMPLE_NAME_MAP.getOrDefault(paramName, "");
        if (StringUtil.isNotBlank(clazzSimpleName)) {
            return;
        }
        List<String> allCodes = ENUM_CODE_MAP.getOrDefault(clazzSimpleName, Collections.emptyList());


        // 添加被比较的值（如 "equals"）
        values.add(comparedValue);

        // 添加一个不同的值（用于测试不等于的情况）
        for (String code : allCodes) {
            if (!code.equals(comparedValue)) {
                values.add(code);
                break;
            }
        }
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
        if (left instanceof NameExpr leftName && right.isFieldAccessExpr()) {
            String paramName = leftName.getNameAsString();
            if (params.contains(paramName)) {
                FieldAccessExpr fieldAccessExpr = right.asFieldAccessExpr();
                addThresholdsByString(paramName, op, fieldAccessExpr, paramValues);
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
    private static void addThresholdsByString(String paramName, BinaryExpr.Operator op, FieldAccessExpr fieldAccessExpr, Map<String, Set<Object>> thresholds) {
        String value = fieldAccessExpr.getNameAsString();
        Set<Object> values = thresholds.get(paramName);
        if (op == BinaryExpr.Operator.EQUALS || op == BinaryExpr.Operator.NOT_EQUALS) {
            NameExpr nameExpr = (NameExpr) fieldAccessExpr.getScope();
            List<String> enumCodeList = ENUM_CODE_MAP.get(nameExpr.getNameAsString());
            //随机取一个非value的值
            for(String enumCode : enumCodeList){
                if(!value.equals(enumCode)){
                    values.add(enumCode);
                    values.add(value.toLowerCase());
                    return ;
                }
            }
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
