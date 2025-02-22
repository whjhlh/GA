package com.whj.generate.utill;

import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.LoggerRuntime;
import org.jacoco.core.runtime.RuntimeData;

import java.io.File;
import java.io.IOException;

/**
 * 覆盖率工具类
 *
 * @author whj
 * @date 2025-02-22 下午7:47
 */
public class JaCocoUtil {
    private static IRuntime runtime;
    private static RuntimeData data;

    static {
        runtime = new LoggerRuntime();
        data = new RuntimeData();
        try {
            runtime.startup(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



    /**
     * 获取当前覆盖率数据
     */
    public static ExecutionDataStore getCurrentCoverage() {
        ExecutionDataStore store = new ExecutionDataStore();
        data.collect(store, null, false);
        return store;
    }

    /**
     * 计算覆盖率
     * @param className 计算类
     * @param coverageBefore 覆盖前
     * @param coverageAfter 覆盖后
     * @return
     * @throws IOException
     */
    public static double analyzeCoverage(Class<?> className, ExecutionDataStore coverageBefore, ExecutionDataStore coverageAfter) throws IOException {
        CoverageBuilder coverageBuilder = new CoverageBuilder();
        Analyzer analyzer = new Analyzer(coverageBefore, coverageBuilder);
        analyzer.analyzeAll(new File("target/classes"));
        //计算行覆盖
        for (IClassCoverage coverage : coverageBuilder.getClasses()) {
            String name = StringUtil.replace(className.getName(), ".", "/");
            if (StringUtil.equals(coverage.getName(), name)) {
                int totalLine = coverage.getLineCounter().getTotalCount();
                int coveredLine = coverage.getLineCounter().getCoveredCount();
                if (totalLine == 0) {
                    return 0.0;
                }
                return (double) coveredLine / totalLine * 100;
            }
        }
        throw new IllegalArgumentException(String.format("【jaCoco】未找到相关类[%s]的覆盖情况",className));
    }
}
