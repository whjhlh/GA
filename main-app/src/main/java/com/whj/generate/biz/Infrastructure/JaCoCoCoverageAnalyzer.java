package com.whj.generate.biz.Infrastructure;

import com.whj.generate.core.domain.Chromosome;
import com.whj.generate.core.domain.Nature;
import com.whj.generate.core.domain.Population;
import com.whj.generate.core.exception.ExceptionWrapper;
import com.whj.generate.core.exception.GenerateErrorEnum;
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
import org.springframework.util.CollectionUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * JaCoCo 覆盖率分析工具类
 *
 * @author whj
 * @date 2025-04-18 上午1:17
 */
public final class JaCoCoCoverageAnalyzer {

    private static final String COVERAGE_ERROR_CONTEXT = "覆盖率分析失败";

    /**
     * 计算种群覆盖率并更新适应度
     *
     * @param nature     环境参数
     * @param population 种群对象
     */
    public static void calculatePopulationCoverage(Nature nature, Population population) {
        final List<byte[]> coverageDataList = new ArrayList<>();
        final Set<Chromosome> chromosomes = population.getChromosomeSet();

        Long totalCoverage = ExceptionWrapper.process(() -> {
            chromosomes.forEach(chromosome ->
                    processChromosome(nature, chromosome, coverageDataList)
            );
            return calculateTotalCoverage(nature, coverageDataList, chromosomes.iterator().next().getMethod());
        }, GenerateErrorEnum.GET_OVERRIDE_FAIL, "种群覆盖率计算失败");

        population.setCurrentCoverage(totalCoverage);
    }

    /**
     * 处理单个染色体覆盖率数据
     */
    private static void processChromosome(Nature nature, Chromosome chromosome, List<byte[]> dataCollector) {
        final Method method = chromosome.getMethod();
        final byte[] coverageData = getOrCollectCoverageData(nature, chromosome, method);

        chromosome.setFitness(calculateChromosomeFitness(nature, chromosome));
        dataCollector.add(coverageData);
    }

    /**
     * 获取或收集染色体覆盖率数据
     */
    private static byte[] getOrCollectCoverageData(Nature nature, Chromosome chromosome, Method method) {
        return Optional.ofNullable(nature.getChromosomeCoverageDataMap())
                .map(map -> map.computeIfAbsent(chromosome, k -> collectNewCoverageData(method, nature, chromosome)))
                .orElseGet(() -> collectNewCoverageData(method, nature, chromosome));
    }

    /**
     * 收集新的覆盖率数据（修正参数传递）
     */
    private static byte[] collectNewCoverageData(Method method, Nature nature, Chromosome chromosome) {
        return ExceptionWrapper.process(() -> {
            final IAgent agent = nature.getAgent();
            agent.reset();

            final Object instance = method.getDeclaringClass().getDeclaredConstructor().newInstance();
            // 修正参数传递逻辑（使用染色体携带的基因数据）
            Object[] params = chromosome.getGenes();
            ReflectionUtil.invokeSafe(method, params, instance);

            return agent.getExecutionData(false);
        }, GenerateErrorEnum.COLLECT_COVERAGE_FAIL, "覆盖率数据收集失败");
    }

    /**
     * 计算染色体适应度
     */
    public static long calculateChromosomeFitness(Nature nature, Chromosome chromosome) {
        final Method method = chromosome.getMethod();
        final byte[] data = getOrCollectCoverageData(nature, chromosome, method);
        return (long) calculateCoveragePercentage(nature, data, method);
    }


    /**
     * 计算总覆盖率
     */
    private static long calculateTotalCoverage(Nature nature, List<byte[]> coverageDataList, Method method) {
        if (CollectionUtils.isEmpty(coverageDataList)) {
            return 0L;
        }

        final ExecutionDataStore mergedStore = mergeCoverageData(coverageDataList);
        return (long) calculateCoveragePercentage(nature, mergedStore, method); // 现在使用正确的重载方法
    }

    /**
     * 从执行数据存储计算覆盖率百分比
     */
    private static double calculateCoveragePercentage(Nature nature, ExecutionDataStore store, Method method) {
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
     * 从字节数据计算覆盖率百分比
     */
    private static double calculateCoveragePercentage(Nature nature, byte[] data, Method method) {
        return ExceptionWrapper.process(() -> {
            final ExecutionDataStore store = new ExecutionDataStore();
            readExecutionData(data, store, new SessionInfoStore());
            return analyzeMethodCoverage(store, method);
        }, GenerateErrorEnum.GET_OVERRIDE_FAIL, COVERAGE_ERROR_CONTEXT);
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
     * 分析方法覆盖率
     */
    private static double analyzeMethodCoverage(ExecutionDataStore store, Method method) throws Exception {
        try (InputStream classStream = getClassByteStream(method)) {
            final CoverageBuilder builder = new CoverageBuilder();
            new Analyzer(store, builder).analyzeClass(classStream, method.getDeclaringClass().getName());

            return builder.getClasses().stream()
                    .flatMap(cc -> cc.getMethods().stream())
                    .filter(mc -> isTargetMethod(mc, method))
                    .findFirst()
                    .map(mc -> calculateCoverageRatio(mc.getLineCounter()))
                    .orElse(0.0);
        }
    }

    /**
     * 获取类字节码流
     */
    private static InputStream getClassByteStream(Method method) {
        final String classResource = method.getDeclaringClass().getName().replace('.', '/') + ".class";
        return method.getDeclaringClass().getClassLoader().getResourceAsStream(classResource);
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