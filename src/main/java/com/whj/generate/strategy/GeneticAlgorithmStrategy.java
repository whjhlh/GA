package com.whj.generate.strategy;

import com.whj.generate.generate.MockitoTestCodeGenerator;
import com.whj.generate.model.Chromosome;
import com.whj.generate.model.TestCase;
import com.whj.generate.utill.GeneticUtil;

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
    public String generateTestCase(Class<?> clazz) {
        List<TestCase> testCases = new ArrayList<>();
        //利用遗传算法生成方法入参
        List<Chromosome> chromosomeList = GeneticUtil.initPopulation(clazz);
        MockitoTestCodeGenerator.generateTestCode(clazz, testCases);
        return MockitoTestCodeGenerator.generateTestCode(clazz, testCases);
    }
}
