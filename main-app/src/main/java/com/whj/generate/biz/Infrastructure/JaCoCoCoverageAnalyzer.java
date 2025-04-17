package com.whj.generate.biz.Infrastructure;

import com.whj.generate.core.bizenum.ComparisonOperatorEnum;
import com.whj.generate.core.bizenum.LogicalOperatorEnum;
import com.whj.generate.core.exception.ExceptionWrapper;
import com.whj.generate.core.exception.GenerateErrorEnum;
import com.whj.generate.whjtest.testForCover;
import org.jacoco.agent.rt.IAgent;
import org.jacoco.agent.rt.internal_aeaf9ab.asm.Type;
import org.jacoco.core.analysis.*;
import org.jacoco.core.data.ExecutionDataReader;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;

import static com.whj.generate.utill.ReflectionUtil.invokeSafe;

/**
 * @author whj
 * @date 2025-04-18 上午1:17
 */
public class JaCoCoCoverageAnalyzer  {
    public static void main(String[] args) {
        testForCover.test(7, 4, 3, LogicalOperatorEnum.OR, ComparisonOperatorEnum.EQUALS);

    }
    public static double getFitness(IAgent agent, Method method, Object[] params)
            throws Exception {
        agent.reset();
        // 1. 创建实例
        Object instance = method.getDeclaringClass().getDeclaredConstructor().newInstance();
        invokeSafe(method, params, instance);

        return JaCoCoCoverageAnalyzer.getLineCoverage(agent, method);
    }

    public static double getLineCoverage(IAgent agent, Method method) {
        return ExceptionWrapper.process(() -> {
            byte[] executionData = agent.getExecutionData(true);

            ExecutionDataStore store = new ExecutionDataStore();
            SessionInfoStore sessionStore = new SessionInfoStore();
            ExecutionDataReader reader = new ExecutionDataReader(new ByteArrayInputStream(executionData));
            reader.setSessionInfoVisitor(sessionStore);
            reader.setExecutionDataVisitor(store);
            reader.read();

            try (InputStream classStream = method.getDeclaringClass()
                    .getClassLoader()
                    .getResourceAsStream(
                            method.getDeclaringClass().getName().replace('.', '/') + ".class")) {
                CoverageBuilder coverageBuilder = new CoverageBuilder();
                Analyzer analyzer = new Analyzer(store, coverageBuilder);
                analyzer.analyzeClass(classStream, method.getDeclaringClass().getName());

                for (IClassCoverage cc : coverageBuilder.getClasses()) {
                    for (IMethodCoverage mc : cc.getMethods()) {
                        if (mc.getName().equals(method.getName())
                                && mc.getDesc().equals(Type.getMethodDescriptor(method))) {

                            int count = 0;
                            for (int i = mc.getFirstLine(); i <= mc.getLastLine(); i++) {
                                ILine line = mc.getLine(i);
                                if(line.getStatus() >=2&& line.getStatus()<=3){
                                    count++;
                                }
                                System.out.printf("Line %d: Status=%s%n", i, line.getStatus());
                            }
                            int total = mc.getLastLine()-mc.getFirstLine();
                            int covered = count;
                            System.out.println("覆盖行数: " + covered + ", 总行数: " + total);
                            return (double) covered / total * 100;
                        }
                    }
                }
                return 0.0;
            }
        }, GenerateErrorEnum.GET_OVERRIDE_FAIL, "获取覆盖失败 %s", method.getName());
    }
}