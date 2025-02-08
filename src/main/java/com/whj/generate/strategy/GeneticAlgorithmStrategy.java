package com.whj.generate.strategy;

import com.whj.generate.model.MethodStructure;
import com.whj.generate.model.TestCase;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GeneticAlgorithmStrategy implements Strategy {

    // 遗传算法参数配置
    private static final int POPULATION_SIZE = 50;
    private static final int MAX_GENERATIONS = 100;
    private static final double MUTATION_RATE = 0.1;
    private static final double CROSSOVER_RATE = 0.8;

    /**
     * 根据方法结构生成测试用例
     *
     * @param method 方法结构信息
     * @return 生成的测试用例
     */
    @Override
    public TestCase generateTestCase(MethodStructure method) {
        try {
            // 1. 获取目标方法信息
            Class<?> targetClass = Class.forName(method.getName().split("\\.")[0]);
            Method targetMethod = targetClass.getMethod(
                    method.getName().split("\\.")[1],
                    method.getParameters().toArray(new Class[0])
            );

            // 2. 执行遗传算法
            List<ParamIndividual> population = initializePopulation(method.getParameters());
            for (int gen = 0; gen < MAX_GENERATIONS; gen++) {
                List<ParamIndividual> newPopulation = new ArrayList<>();

                // 评估适应度
                Map<ParamIndividual, Double> fitness = calculateFitness(population);

                // 精英保留
                newPopulation.add(Collections.max(fitness.entrySet(),
                        Map.Entry.comparingByValue()).getKey());

                // 生成新种群
                while (newPopulation.size() < POPULATION_SIZE) {
                    // 选择
                    ParamIndividual parent1 = selectParent(fitness);
                    ParamIndividual parent2 = selectParent(fitness);

                    // 交叉
                    if (Math.random() < CROSSOVER_RATE) {
                        ParamIndividual[] children = crossover(parent1, parent2);
                        newPopulation.add(children[0]);
                        newPopulation.add(children[1]);
                    } else {
                        newPopulation.add(parent1);
                        newPopulation.add(parent2);
                    }
                }

                // 变异
                newPopulation = newPopulation.stream()
                        .map(this::mutate)
                        .collect(Collectors.toList());

                population = newPopulation;
            }

            // 3. 生成最终测试用例
            ParamIndividual best = Collections.max(calculateFitness(population).entrySet(),
                    Map.Entry.comparingByValue()).getKey();

            return buildTestCase(targetMethod, best);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 初始化种群
    private List<ParamIndividual> initializePopulation(List<Class<?>> paramTypes) {
        return IntStream.range(0, POPULATION_SIZE)
                .mapToObj(i -> generateRandomIndividual(paramTypes))
                .collect(Collectors.toList());
    }

    // 生成随机个体
    private ParamIndividual generateRandomIndividual(List<Class<?>> paramTypes) {
        List<Object> params = paramTypes.stream()
                .map(type -> {
                    if (type.equals(int.class) || type.equals(Integer.class)) {
                        return new Random().nextInt(100) - 50; // -50到49的随机数
                    } else if (type.equals(String.class)) {
                        return generateRandomString();
                    } else if (type.equals(boolean.class)) {
                        return new Random().nextBoolean();
                    }
                    return null; // 其他类型需要扩展
                })
                .collect(Collectors.toList());
        return new ParamIndividual(params);
    }

    // 适应度计算（示例：鼓励参数多样性）
    private Map<ParamIndividual, Double> calculateFitness(List<ParamIndividual> population) {
        return population.stream()
                .collect(Collectors.toMap(
                        ind -> ind,
                        ind -> ind.parameters.stream()
                                .mapToDouble(this::parameterDiversityScore)
                                .sum()
                ));
    }

    // 参数多样性评分（示例实现）
    private double parameterDiversityScore(Object param) {
        if (param instanceof Integer) {
            int val = (Integer) param;
            // 鼓励边界值
            if (val == 0 || val == Integer.MAX_VALUE || val == Integer.MIN_VALUE)
                return 2.0;
            return 1.0 / (1 + Math.abs(val)); // 鼓励极端值
        }
        if (param instanceof String) {
            String s = (String) param;
            if (s.isEmpty()) return 2.0;
            if (s.length() > 100) return 1.5;
            return 1.0;
        }
        return 1.0;
    }

    // 轮盘赌选择
    private ParamIndividual selectParent(Map<ParamIndividual, Double> fitness) {
        double total = fitness.values().stream().mapToDouble(Double::doubleValue).sum();
        double threshold = new Random().nextDouble() * total;

        double accum = 0;
        for (Map.Entry<ParamIndividual, Double> entry : fitness.entrySet()) {
            accum += entry.getValue();
            if (accum >= threshold) {
                return entry.getKey();
            }
        }
        return fitness.keySet().iterator().next();
    }

    // 单点交叉
    private ParamIndividual[] crossover(ParamIndividual p1, ParamIndividual p2) {
        int point = new Random().nextInt(p1.parameters.size());

        List<Object> child1 = new ArrayList<>();
        List<Object> child2 = new ArrayList<>();

        for (int i = 0; i < p1.parameters.size(); i++) {
            if (i < point) {
                child1.add(p1.parameters.get(i));
                child2.add(p2.parameters.get(i));
            } else {
                child1.add(p2.parameters.get(i));
                child2.add(p1.parameters.get(i));
            }
        }
        return new ParamIndividual[]{
                new ParamIndividual(child1),
                new ParamIndividual(child2)
        };
    }

    // 变异操作
    private ParamIndividual mutate(ParamIndividual individual) {
        if (Math.random() > MUTATION_RATE) return individual;

        List<Object> newParams = new ArrayList<>(individual.parameters);
        int mutationPoint = new Random().nextInt(newParams.size());

        Class<?> paramType = newParams.get(mutationPoint).getClass();
        Object mutatedValue = generateRandomValue(paramType);
        newParams.set(mutationPoint, mutatedValue);

        return new ParamIndividual(newParams);
    }

    // 生成测试用例代码
    private TestCase buildTestCase(Method method, ParamIndividual best) {
        String methodName = method.getName();
        String className = method.getDeclaringClass().getSimpleName();
        List<Class<?>> imports = Collections.singletonList(method.getDeclaringClass());

        // 构造参数列表
        String params = best.parameters.stream()
                .map(p -> {
                    if (p instanceof String) return "\"" + p + "\"";
                    return String.valueOf(p);
                })
                .collect(Collectors.joining(", "));

        // 构造测试方法体
        String body = "// Generated by Genetic Algorithm\n" +
                "try {\n" +
                "    " + className + " instance = new " + className + "();\n" +
                "    Object result = instance." + methodName + "(" + params + ");\n" +
                "    // Add assertions here\n" +
                "} catch (Exception e) {\n" +
                "    // Handle exceptions\n" +
                "}";

        return new TestCase(
                imports,
                "test" + methodName + "GA",
                method,
                body
        );
    }

    // 辅助方法：生成随机字符串
    private String generateRandomString() {
        int length = new Random().nextInt(20);
        return new Random().ints(97, 123)
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    // 辅助方法：生成随机参数值
    private Object generateRandomValue(Class<?> type) {
        Random rand = new Random();
        if (type.equals(int.class) || type.equals(Integer.class)) {
            return rand.nextInt(200) - 100;
        } else if (type.equals(String.class)) {
            return generateRandomString();
        } else if (type.equals(boolean.class)) {
            return rand.nextBoolean();
        }
        return null;
    }

    // 辅助类：参数个体表示
    private static class ParamIndividual {
        List<Object> parameters;

        ParamIndividual(List<Object> params) {
            this.parameters = params;
        }
    }
}
