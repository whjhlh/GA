package com.whj.generate;

import com.whj.generate.biz.Infrastructure.JaCoCoCoverageAnalyzer;
import com.whj.generate.core.bizenum.ComparisonOperatorEnum;
import com.whj.generate.core.bizenum.LogicalOperatorEnum;
import com.whj.generate.whjtest.testForCover;
import org.jacoco.agent.rt.IAgent;
import org.jacoco.agent.rt.RT;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import java.lang.reflect.Method;

import static org.springframework.boot.SpringApplication.run;

/**
 * @author whj
 * @date 2025-04-10 上午2:51
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.whj.generate")
public class GeneticApplication {
        public static void main(String[] args) {


            try {

                System.setProperty("spring.devtools.restart.enabled", "false");
                System.setProperty("spring.devtools.restart.quiet-period", "0");
                System.out.println("程序启动中...");
                ApplicationContext context = run(GeneticApplication.class, args);
                System.out.println("程序启动成功!");
                testForCover testForCover = new testForCover();
                Class<? extends com.whj.generate.whjtest.testForCover> aClass = testForCover.getClass();
                Method[] methods = aClass.getMethods();
                for(Method method : methods){
                    if(method.getName().equals("test")){
                        IAgent agent = RT.getAgent(); // 获取JaCoCo Agent实例
                        agent.reset();
                        com.whj.generate.whjtest.testForCover.test(7, 4, 3, LogicalOperatorEnum.OR, ComparisonOperatorEnum.EQUALS);
                        double lineCoverage = JaCoCoCoverageAnalyzer.getLineCoverage(agent, method);

                        System.out.println(lineCoverage);
                    }
                }
            } catch (Exception e) {
                System.err.println("程序启动失败:");
                e.printStackTrace();
            }
        }

    }

