package com.whj.generate.core.infrastructure.impl;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.ast.stmt.IfStmt;
import com.whj.generate.core.infrastructure.ParamThresholdExtractor;
import com.whj.generate.whjtest.TestForCover;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.whj.generate.utill.EnumScannerUtil.findEnumsInMethod;
import static com.whj.generate.utill.ListUtil.defualtList;
import static com.whj.generate.utill.PathUtils.getFilePath;
import static com.whj.generate.utill.ReflectionUtil.getJavaCode;
import static com.whj.generate.utill.UltraFastParamScannerUtil.scanParamsUltraFast;

/**
 * 条件提取器
 * @author whj
 * @date 2025-04-04 下午5:16
 */
@Component
public class GeneThresholdExtractor implements ParamThresholdExtractor {
    /**
     * 缓存反向操作符
     */
    private static final Map<BinaryExpr.Operator, BinaryExpr.Operator> REVERSE_OP_CACHE = new EnumMap<>(BinaryExpr.Operator.class);
    /**
     * 枚举类与 code 值
     */
    private static final Map<String, List<String>> ENUM_CODE_MAP;
    /**
     * 缓存编译单元
     */
    private static final Map<String, CompilationUnit> FILE_CACHE = new ConcurrentHashMap<>();
    /**
     * 参数名与简单名称的映射
     */
    private static Map<String, String> PARAM_NAME_SIMPLE_NAME_MAP;
    /**
     * 参数常量
     */
    private static List<String> paramsConst;

    /**
     * 参数边界值
     */
    private static final Map<String, Set<Object>> thresholdsMap = new HashMap<>();

    static {
        REVERSE_OP_CACHE.put(BinaryExpr.Operator.GREATER, BinaryExpr.Operator.LESS_EQUALS);
        REVERSE_OP_CACHE.put(BinaryExpr.Operator.LESS, BinaryExpr.Operator.GREATER_EQUALS);
        REVERSE_OP_CACHE.put(BinaryExpr.Operator.GREATER_EQUALS, BinaryExpr.Operator.LESS);
        REVERSE_OP_CACHE.put(BinaryExpr.Operator.LESS_EQUALS, BinaryExpr.Operator.GREATER);
        REVERSE_OP_CACHE.put(BinaryExpr.Operator.EQUALS, BinaryExpr.Operator.NOT_EQUALS);
        try {
            ENUM_CODE_MAP = findEnumsInMethod(TestForCover.class, "test");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public Map<String, Set<Object>> extractThresholds(Class<?> targetClass, String methodName) {
        Map<String, Set<Object>> paramValues = new HashMap<>();

        CompilationUnit cu = getCompilationUnit(targetClass,methodName);
        cu.getClassByName(targetClass.getSimpleName()).ifPresent(classDecl -> {
            classDecl.getMethodsByName(methodName).forEach(method -> {
                paramsConst=getParams(method);
                List<String> params = paramsConst;
                params.forEach(param -> paramValues.put(param, new HashSet<>()));
                method.findAll(IfStmt.class).forEach(ifStmt -> {
                    List<Expression> atomicConditions = new ArrayList<>();
                    flattenConditions(ifStmt.getCondition(), atomicConditions);
                    atomicConditions.forEach(expr -> extractThresholds(expr, params, paramValues));
                });
            });
        });

        // 步骤2: 对每个参数进行阈值优化处理
        paramValues.forEach((paramKey, valueSet) -> {
            if (!thresholdsMap.containsKey(paramKey)) return;

            // 类型安全转换（仅处理整型参数）
            List<Integer> thresholds = convertToIntegerList(thresholdsMap.get(paramKey));
            List<Integer> conditions = convertToIntegerList(valueSet);

            if (thresholds.isEmpty()) {
                valueSet.clear();
                return;
            }

            // 阈值处理核心逻辑
            Set<Object> optimizedValues = processThresholds(thresholds, conditions);
            paramValues.put(paramKey, optimizedValues);
        });

        return paramValues;
    }

    /**
     * 获取方法参数
     * @param method
     * @return
     */
    @Override
    public List<String> resolveParameterNames(Method method) {
        return Collections.unmodifiableList(paramsConst);
    }
    private static List<String> getParams(MethodDeclaration method) {
        return method.getParameters().stream()
                .map(NodeWithSimpleName::getNameAsString)
                .toList();
    }

    private static CompilationUnit getCompilationUnit(Class<?> targetClass, String methodName) {
        String filePath = getFilePath(targetClass, StandardCharsets.UTF_8);
        String javaCode = null;
        try {
            javaCode = getJavaCode(targetClass, filePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        PARAM_NAME_SIMPLE_NAME_MAP = scanParamsUltraFast(javaCode, methodName);
        return FILE_CACHE.computeIfAbsent(filePath, p -> {
            try {
                return StaticJavaParser.parse(new File(p));
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static void handledThresholdsByInt(List<String> params, Map<String, Set<Object>> paramValues, Expression left, Expression right, BinaryExpr.Operator op) {
        if (left instanceof NameExpr leftName && right.isIntegerLiteralExpr()) {
            String paramName = leftName.getNameAsString();
            if (params.contains(paramName)) {
                int value = right.asIntegerLiteralExpr().asNumber().intValue();
                if(thresholdsMap.containsKey(paramName)){
                    thresholdsMap.get(paramName).add(value);
                }else {
                    thresholdsMap.put(paramName,new HashSet<>());
                }
                addThresholdsByInt(paramName, op, value, paramValues);
            }
        }
    }

    // 类型安全转换方法（防御性编程）
    private List<Integer> convertToIntegerList(Collection<Object> collection) {
        return collection.stream()
                .filter(Integer.class::isInstance)
                .map(Integer.class::cast)
                .sorted()
                .collect(Collectors.toList());
    }

    public static List<String> getParamsConst() {
        return paramsConst;
    }

    /**
     * 扁平化条件
     *
     * @param expr
     * @param atomicConditions
     */
    private static void flattenConditions(Expression expr, List<Expression> atomicConditions) {
        Deque<Expression> stack = new ArrayDeque<>();
        stack.push(expr);

        while (!stack.isEmpty()) {
            Expression current = stack.pop();
            if (current instanceof BinaryExpr be && (be.getOperator() == BinaryExpr.Operator.AND || be.getOperator() == BinaryExpr.Operator.OR)) {
                stack.push(be.getRight());
                stack.push(be.getLeft());
            } else {
                atomicConditions.add(current);
            }
        }
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
        } else if (expr instanceof MethodCallExpr methodCall) {
            handleEnumMethodCall(methodCall, params, paramValues);
        }

    }

    private static void handleEnumMethodCall(MethodCallExpr methodCall, List<String> params, Map<String, Set<Object>> paramValues) {
        // 仅处理 equals 方法调用
        if (!methodCall.getNameAsString().equals("equals")) {
            return;
        }

        // 检查是否为 enmu.getCode().equals("equals") 结构
        if (methodCall.getScope().isEmpty()) return;
        if (methodCall.getScope().get() instanceof MethodCallExpr scopeCall) {
            // 确认是 getCode() 方法调用
            if (!scopeCall.getNameAsString().equals("getCode")) return;
            if (scopeCall.getScope().isEmpty()) return;
            if (scopeCall.getScope().get() instanceof NameExpr enumNameExpr) {

                String paramName = enumNameExpr.getNameAsString();
                if (!params.contains(paramName)) return;
                // 获取 equals 的参数值（如 "equals"）
                if (methodCall.getArgument(0).isStringLiteralExpr()) {
                    String comparedValue = methodCall.getArgument(0).asStringLiteralExpr().getValue();
                    // 添加枚举的 code 值到结果集
                    addEnumCodeThresholds(paramName, comparedValue, paramValues);
                }

            }
        }
    }

    private static void addEnumCodeThresholds(String paramName, String comparedValue, Map<String, Set<Object>> paramValues) {
        Set<Object> values = paramValues.computeIfAbsent(paramName, k -> new HashSet<>());
        // 添加被比较的值（如 "equals"）
        values.add(comparedValue);

        // 从预加载的 ENUM_CODE_MAP 中获取该枚举的所有 code 值
        String clazzSimpleName = PARAM_NAME_SIMPLE_NAME_MAP.get(paramName);
        List<String> allCodes = ENUM_CODE_MAP.getOrDefault(clazzSimpleName, Collections.emptyList());

        // 添加一个不同的值（用于测试不等于的情况）
        Optional<String> firstCode = allCodes.parallelStream()
                .filter(code -> !code.equals(comparedValue))
                .findFirst();
        firstCode.ifPresent(values::add);

    }


    /**
     * 处理枚举类型
     *
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

    // 阈值处理核心逻辑封装
    private Set<Object> processThresholds(List<Integer> thresholds, List<Integer> conditions) {
        // 步骤1: 基础处理
        Collections.sort(thresholds);

        // 步骤2: 生成插入值
        List<Integer> insertions = IntStream.range(0, thresholds.size()-1)
                .mapToObj(i -> {
                    int left = thresholds.get(i);
                    int right = thresholds.get(i+1);
                    return right - left > 1 ? left + 1 : null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 步骤3: 合并数据集
        Set<Integer> combined = new TreeSet<>(conditions);
        combined.addAll(insertions);

        // 步骤4: 边界确定
        int minThreshold = thresholds.get(0);
        int maxThreshold = thresholds.get(thresholds.size()-1);

        // 步骤5: 结果筛选
        Set<Object> result = new LinkedHashSet<>();
        Optional<Integer> maxExceeding = combined.stream()
                .filter(n -> n > maxThreshold)
                .max(Integer::compare);

        combined.forEach(num -> {
            if (num < minThreshold ||
                    thresholds.contains(num) ||
                    insertions.contains(num)) {
                result.add(num);
            }
        });

        maxExceeding.ifPresent(result::add);
        return result;
    }

    private static void addThresholdsByString(String paramName, BinaryExpr.Operator op, FieldAccessExpr fieldAccessExpr, Map<String, Set<Object>> thresholds) {
        String value = fieldAccessExpr.getNameAsString();
        Set<Object> values = thresholds.get(paramName);
        if (op == BinaryExpr.Operator.EQUALS || op == BinaryExpr.Operator.NOT_EQUALS) {
            NameExpr nameExpr = (NameExpr) fieldAccessExpr.getScope();
            List<String> enumCodeList = ENUM_CODE_MAP.get(nameExpr.getNameAsString());
            //随机取一个非value的值
            for (String enumCode : defualtList(enumCodeList)) {
                if (!value.equals(enumCode)) {
                    values.add(enumCode);
                    values.add(value.toLowerCase());
                    return;
                }
            }
        }
    }

    /**
     * 检查操作符
     *
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
     *
     * @param paramName
     * @param op
     * @param value
     * @param thresholds
     */
    private static void addThresholdsByInt(String paramName, BinaryExpr.Operator op, int value, Map<String, Set<Object>> thresholds) {
        Set<Object> values = thresholds.get(paramName);
        switch (op) {
            case EQUALS, NOT_EQUALS -> {
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
            default -> {
            }
        }
    }

}
