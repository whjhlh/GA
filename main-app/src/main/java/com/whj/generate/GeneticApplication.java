package com.whj.generate;

import com.whj.generate.common.GeneticAlgorithmController;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import static org.springframework.boot.SpringApplication.run;

/**
 * @author whj
 * @date 2025-04-10 上午2:51
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.whj.generate")
public class GeneticApplication {
    public static void main(String[] args) {
        //
        //TestForCover.test(8,11,13,LogicalOperatorEnum.AND,ComparisonOperatorEnum.EQUALS);
        ApplicationContext context = run(GeneticApplication.class, args);
        // 2. 从容器获取Controller实例
        GeneticAlgorithmController controller = context.getBean(GeneticAlgorithmController.class);
        controller.runGeneticAlgorithm();
        System.out.println("结束");
    }
}

