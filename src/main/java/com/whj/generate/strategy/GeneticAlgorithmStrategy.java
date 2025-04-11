package com.whj.generate.strategy;

import com.whj.generate.core.domain.TestCase;
import com.whj.generate.generate.MockitoTestCodeGenerator;
import com.whj.generate.utill.ReflectionUtil;

import java.util.ArrayList;
import java.util.List;

public class GeneticAlgorithmStrategy implements Strategy {

    // 遗传算法参数配置
    private static final int POPULATION_SIZE = 50;
    private static final int MAX_GENERATIONS = 100;
    private static final double MUTATION_RATE = 0.1;
    private static final double CROSSOVER_RATE = 0.8;

    /**
     * 根据方法结构生成测试用例
     *
     * @param clazz 方法结构信息
     * @return 生成的测试用例
     */
    @Override
    public String generateTestCase(Class<?> clazz) throws Exception {
        //todo 暂时不支持(final类和final方法则不生成测试用例)
        //todo 后续开发切片模式
        if (clazz == null || ReflectionUtil.isFinalClass(clazz)) {
            return null;
        }

        List<TestCase> testCases = new ArrayList<>();
        //【1】利用遗传算法生成方法入参
        //【2】覆盖率分析
//        Object coverageProxy =ProxyUtil.createCoverageProxy(clazz);
//        //【3】执行方法跑用例
//        for (Chromosome chromosome : chromosomeList) {
//            ReflectionUtil.invokeMethod(coverageProxy, chromosome.getMethod(), chromosome.getGenes());
//        }
        //【4】todo 利用覆盖率分析结果进行交叉变异

        //【4】利用testCase生成测试用例代码
        return MockitoTestCodeGenerator.generateTestCode(clazz, testCases);
    }

}
