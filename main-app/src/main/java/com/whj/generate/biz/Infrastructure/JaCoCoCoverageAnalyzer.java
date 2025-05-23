package com.whj.generate.biz.Infrastructure;

import com.whj.generate.biz.Infrastructure.cache.ChromosomeCoverageTracker;
import com.whj.generate.core.domain.Chromosome;
import com.whj.generate.core.exception.ExceptionWrapper;
import com.whj.generate.core.exception.GenerateErrorEnum;
import com.whj.generate.utill.InputStreamUtil;
import com.whj.generate.utill.ReflectionUtil;
import org.jacoco.agent.rt.IAgent;
import org.jacoco.agent.rt.internal_aeaf9ab.asm.Type;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.IMethodCoverage;
import org.jacoco.core.data.ExecutionDataReader;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.List;

/**
 * JaCoCo 覆盖率分析工具类
 *
 * @author whj
 * @date 2025-04-18 上午1:17
 */
@Component
public final class JaCoCoCoverageAnalyzer {
    /**
     * jaCocoAgent
     */
    private final IAgent agent;
    /**
     * 覆盖率追踪器
     */
    private final ChromosomeCoverageTracker coverageTracker;

    private static final String COVERAGE_ERROR_CONTEXT = "覆盖率分析失败";

    @Autowired
    public JaCoCoCoverageAnalyzer(IAgent agent, ChromosomeCoverageTracker coverageTracker) {
        this.agent = agent;
        this.coverageTracker = coverageTracker;
    }


    /**
     * 收集新的覆盖率数据（修正参数传递）
     */
    public byte[] collectNewCoverageData(Chromosome chromosome) {
        // 执行代码并收集覆盖率数据
        return ExceptionWrapper.process(() -> {
            Object[] params = chromosome.getGenes();
            final Method method = chromosome.getMethod();
            agent.reset();
            final Object instance = method.getDeclaringClass().getDeclaredConstructor().newInstance();
            //真实调用逻辑
            String errorMsg = ReflectionUtil.invokeSafe(method, params, instance);
            if (errorMsg != null) {
                chromosome.setErrorMsg(errorMsg);
            }
            return agent.getExecutionData(false);
        }, GenerateErrorEnum.COLLECT_COVERAGE_FAIL, "覆盖率数据收集失败");
    }


    /**
     * 带染色体追踪的覆盖率计算
     */
    public double calculateCoveragePercentage(byte[] data, Chromosome chromosome) {
        return ExceptionWrapper.process(() -> {
            final ExecutionDataStore store = new ExecutionDataStore();
            readExecutionData(data, store, new SessionInfoStore());
            return analyzeMethodCoverage(store, chromosome, chromosome.getMethod());
        }, GenerateErrorEnum.GET_OVERRIDE_FAIL, "覆盖率计算失败");
    }

    /**
     * 带染色体追踪的分析方法
     */
    private double analyzeMethodCoverage(ExecutionDataStore store,
                                         Chromosome chromosome, Method method) throws Exception {
        try (InputStream classStream = InputStreamUtil.getClassByteStream(method)) {
            final CoverageBuilder builder = new CoverageBuilder();
            Analyzer analyzer = new Analyzer(store, builder);
            analyzer.analyzeClass(classStream, method.getDeclaringClass().getName());

            // 处理类覆盖率数据
            builder.getClasses().forEach(cc -> {
                coverageTracker.processClassCoverage(cc, method, chromosome);
            });
            // 返回所有类中目标方法的最大覆盖率比率
            return builder.getClasses().stream()
                    // 将每个类的方法流合并为一个流
                    .flatMap(cc -> cc.getMethods().stream())
                    // 过滤出目标方法
                    .filter(mc -> isTargetMethod(mc, method))
                    // 计算每个目标方法的覆盖率比率并转换为Double流
                    .mapToDouble(mc -> calculateCoverageRatio(mc.getLineCounter()))
                    // 找出最大的覆盖率比率
                    .max()
                    // 如果没有目标方法，则默认返回0.0
                    .orElse(0.0);
        }
        
    }


    /**
     * 分析方法覆盖率
     */
    private static double analyzeMethodCoverage(ExecutionDataStore store, Method method) throws Exception {
        try (InputStream classStream = InputStreamUtil.getClassByteStream(method)) {
            final CoverageBuilder builder = new CoverageBuilder();
            Analyzer analyzer = new Analyzer(store, builder);
            analyzer.analyzeClass(classStream, method.getDeclaringClass().getName());

            return builder.getClasses().stream()
                    .flatMap(cc -> cc.getMethods().stream())
                    .filter(mc -> isTargetMethod(mc, method))
                    .findFirst()
                    .map(mc -> calculateCoverageRatio(mc.getLineCounter()))
                    .orElse(0.0);
        }
    }

    /**
     * 计算总覆盖率
     */
    public long calculateTotalCoverage(List<byte[]> coverageDataList, Method method) {
        if (CollectionUtils.isEmpty(coverageDataList)) {
            return 0L;
        }

        final ExecutionDataStore mergedStore = mergeCoverageData(coverageDataList);
        return (long) calculateCoveragePercentage(mergedStore, method);
    }


    /**
     * 从执行数据存储计算覆盖率百分比
     */
    private static double calculateCoveragePercentage(ExecutionDataStore store, Method method) {
        return ExceptionWrapper.process(() ->
                        analyzeMethodCoverage(store, method),
                GenerateErrorEnum.GET_OVERRIDE_FAIL,
                COVERAGE_ERROR_CONTEXT
        );
    }

    /**
     * 合并多个覆盖率数据集
     */
    private static ExecutionDataStore mergeCoverageData(List<byte[]> dataList) {
        final ExecutionDataStore store = new ExecutionDataStore();
        dataList.forEach(data -> readExecutionData(data, store, new SessionInfoStore()));
        return store;
    }

    /**
     * 读取执行数据到存储对象
     */
    private static void readExecutionData(byte[] data, ExecutionDataStore dataStore, SessionInfoStore sessionStore) {
        ExecutionDataReader reader = new ExecutionDataReader(new ByteArrayInputStream(data));
        reader.setExecutionDataVisitor(dataStore);
        reader.setSessionInfoVisitor(sessionStore);
        try {
            reader.read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 判断是否为目标方法
     */
    private static boolean isTargetMethod(IMethodCoverage mc, Method method) {
        return mc.getName().equals(method.getName())
                && mc.getDesc().equals(Type.getMethodDescriptor(method));
    }

    /**
     * 计算覆盖率比例
     */
    private static double calculateCoverageRatio(ICounter counter) {
        return counter.getTotalCount() == 0 ? 0.0 :
                counter.getCoveredCount() * 100.0 / counter.getTotalCount();
    }
}
