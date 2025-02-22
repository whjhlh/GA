package com.whj.generate;

import com.whj.generate.actual.QueryController;
import com.whj.generate.strategy.GeneticAlgorithmStrategy;
import com.whj.generate.strategy.Strategy;

/**
 *
 *
 * @author whj
 * @date 2025-01-01 下午7:09
 */
public class MainSystem {
    public static void main(String[] args) throws NoSuchMethodException {
        Strategy strategy = new GeneticAlgorithmStrategy();
        String code = strategy.generateTestCase(QueryController.class);
        // 可选择保存到文件
        //CodeSaverUtil.saveToFile("GeneratedTest.java", testCode);

    }
}
