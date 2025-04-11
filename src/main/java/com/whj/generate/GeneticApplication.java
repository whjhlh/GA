package com.whj.generate;

import com.whj.generate.common.GeneticAlgorithmController;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import static org.springframework.boot.SpringApplication.run;

/**
 * @author whj
 * @date 2025-04-10 上午2:51
 */
@SpringBootApplication
public class GeneticApplication {
    public static void main(String[] args) {
        ApplicationContext context = run(GeneticApplication.class, args);
        // 2. 从容器获取Controller实例
        GeneticAlgorithmController controller = context.getBean(GeneticAlgorithmController.class);

        // 3. 调用方法
        controller.test();
    }
}
