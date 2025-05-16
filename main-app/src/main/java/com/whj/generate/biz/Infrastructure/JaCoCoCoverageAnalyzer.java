package com.whj.generate.biz.Infrastructure;

import com.whj.generate.biz.Infrastructure.cache.ChromosomeCoverageTracker;
import com.whj.generate.core.domain.Chromosome;
import com.whj.generate.core.domain.Nature;
import com.whj.generate.core.domain.Population;
import com.whj.generate.core.exception.ExceptionWrapper;
import com.whj.generate.core.exception.GenerateErrorEnum;
import com.whj.generate.core.service.FitnessCalculatorService;
import com.whj.generate.utill.ReflectionUtil;
import org.jacoco.agent.rt.IAgent;
import org.jacoco.agent.rt.internal_aeaf9ab.asm.Type;
import org.jacoco.core.analysis.*;
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
@Component
public final class JaCoCoCoverageAnalyzer {
    // 覆盖率追踪器实例（线程安全）
    private static final ChromosomeCoverageTracker coverageTracker = new ChromosomeCoverageTracker();

    private static final String COVERAGE_ERROR_CONTEXT = "覆盖率分析失败";

    @Autowired
    private final FitnessCalculatorService fitnessCalculator;

    public JaCoCoCoverageAnalyzer(FitnessCalculatorService fitnessCalculator) {
        this.fitnessCalculator = fitnessCalculator;
    }

    /**
     * 获取或收集染色体覆盖率数据
     */
    private static byte[] getOrCollectCoverageData(Nature nature, Chromosome chromosome, Method method) {
        // naturemap中获取或计算覆盖率数据
        return Optional.ofNullable(nature.getChromosomeCoverageDataMap())
                .map(map -> map.computeIfAbsent(chromosome, k -> collectNewCoverageData(method, nature, chromosome)))
                .orElseGet(() -> collectNewCoverageData(method, nature, chromosome));
    }

    /**
     * 处理单个染色体覆盖率数据
     */
    private static void processChromosome(Nature nature, Chromosome chromosome, List<byte[]> dataCollector) {
        final Method method = chromosome.getMethod();
        final byte[] coverageData = getOrCollectCoverageData(nature, chromosome, method);

        chromosome.setCoveragePercent(calculateChromosomePercentage(nature, chromosome));
        dataCollector.add(coverageData);
    }

    /**
     * 收集新的覆盖率数据（修正参数传递）
     */
    private static byte[] collectNewCoverageData(Method method, Nature nature, Chromosome chromosome) {
        // 执行代码并收集覆盖率数据
        return ExceptionWrapper.process(() -> {
            final IAgent agent = nature.getAgent();
            agent.reset();

            final Object instance = method.getDeclaringClass().getDeclaredConstructor().newInstance();
            // 修正参数传递逻辑（使用染色体携带的基因数据）
            Object[] params = chromosome.getGenes();
            //真实调用逻辑
            ReflectionUtil.invokeSafe(method, params, instance);

            return agent.getExecutionData(false);
        }, GenerateErrorEnum.COLLECT_COVERAGE_FAIL, "覆盖率数据收集失败");
    }

    /**
     * 计算染色体适应度（新增追踪逻辑）
     */
    public static long calculateChromosomePercentage(Nature nature, Chromosome chromosome) {
        final Method method = chromosome.getMethod();
        final byte[] data = getOrCollectCoverageData(nature, chromosome, method);
        final double coverage = calculateCoveragePercentage(nature, data, chromosome);
        return (long) coverage;
    }

    /**
     * 带染色体追踪的覆盖率计算
     */
    private static double calculateCoveragePercentage(Nature nature, byte[] data, Chromosome chromosome) {
        return ExceptionWrapper.process(() -> {
            final ExecutionDataStore store = new ExecutionDataStore();
            readExecutionData(data, store, new SessionInfoStore());
            return analyzeMethodCoverage(store, chromosome);
        }, GenerateErrorEnum.GET_OVERRIDE_FAIL, "覆盖率计算失败");
    }

    /**
     * 带染色体追踪的分析方法
     */
    private static double analyzeMethodCoverage(ExecutionDataStore store,
                                                Chromosome chromosome) throws Exception {
        final Method method = chromosome.getMethod();
        try (InputStream classStream = getClassByteStream(method)) {
            final CoverageBuilder builder = new CoverageBuilder();
            Analyzer analyzer = new Analyzer(store, builder);
            analyzer.analyzeClass(classStream, method.getDeclaringClass().getName());

            // 处理类覆盖率数据
            builder.getClasses().forEach(cc -> {
                processClassCoverage(cc, method, chromosome);
            });

            return calculateMaxCoverage(builder, method);
        }
    }

    private static void processClassCoverage(IClassCoverage classCoverage, Method method, Chromosome chromosome) {
        // 记录每个覆盖行与染色体的映射关系
        List<Integer> coveredLines = new ArrayList<>();

        classCoverage.getMethods().stream()
                .filter(mc -> isTargetMethod(mc, method))
                .forEach(mc -> {
                    for (int i = mc.getFirstLine(); i <= mc.getLastLine(); i++) {
                        coverageTracker.init(mc.getFirstLine(), mc.getLastLine());
                        if (mc.getLine(i).getStatus() == ICounter.FULLY_COVERED || mc.getLine(i).getStatus() == ICounter.PARTLY_COVERED) {
                            coveredLines.add(i);
                        }
                    }
                });

        // 调用追踪器的记录方法

        coverageTracker.recordCoverage(coveredLines, chromosome);
    }

    /**
     * 计算最大覆盖率值
     */
    private static double calculateMaxCoverage(CoverageBuilder builder, Method method) {
        return builder.getClasses().stream()
                .flatMap(cc -> cc.getMethods().stream())
                .filter(mc -> isTargetMethod(mc, method))
                .mapToDouble(mc -> calculateCoverageRatio(mc.getLineCounter()))
                .max()
                .orElse(0.0);
    }

    /**
     * 计算总覆盖率
     */
    private static long calculateTotalCoverage(Nature nature, List<byte[]> coverageDataList, Method method) {
        if (CollectionUtils.isEmpty(coverageDataList)) {
            return 0L;
        }

        final ExecutionDataStore mergedStore = mergeCoverageData(coverageDataList);
        return (long) calculateCoveragePercentage(nature, mergedStore, method);
    }

    /**
     * 计算种群覆盖率并更新适应度
     *
     * @param nature     环境参数
     * @param population 种群对象
     */
    public void calculatePopulationCoverage(Nature nature, Population population) {
        if (population == null) {
            return;
        }
        Set<Integer> finalLastPopulationUnCovered = getLastPopulationUnCovered(nature);
        // 初始化覆盖率追踪器（线程安全）
        final List<byte[]> coverageDataList = new ArrayList<>();
        // 获取每个染色体的覆盖率数据
        final Set<Chromosome> chromosomes = population.getChromosomeSet();
        // 遍历每个染色体，计算覆盖率并更新适应度
        Long totalCoverage = ExceptionWrapper.process(() -> {
            chromosomes.forEach(chromosome -> {
                        // 处理单个染色体覆盖率数据
                        processChromosome(nature, chromosome, coverageDataList);
                        //获取未覆盖行
                        Set<Integer> uncovered = getUncovered(nature, population, finalLastPopulationUnCovered);
                        fitnessCalculator.calculate(chromosome, coverageTracker, uncovered);
                    }
            );
            return calculateTotalCoverage(nature, coverageDataList, chromosomes.iterator().next().getMethod());
        }, GenerateErrorEnum.GET_OVERRIDE_FAIL, "种群覆盖率计算失败");
        population.setCurrentCoverage(totalCoverage);
    }

    /**
     * 获取上次种群未覆盖行
     * @param nature
     * @return
     */
    private static Set<Integer> getLastPopulationUnCovered(Nature nature) {
        Set<Integer> lastPopulationUnCovered = Set.of();
        if (isNotInitPopulation(nature)) {
            //获取上个种群
            int size = nature.getPopulationList().size();
            final Population lastPopulation = nature.getPopulationList().get(size-1);
            lastPopulationUnCovered = coverageTracker.getPopulationUnCoveredLines(lastPopulation);
        }
        return lastPopulationUnCovered;
    }
    /**
     * 获取当前种群未覆盖行
     */
    private static Set<Integer> getUncovered(Nature nature, Population population, Set<Integer> finalLastPopulationUnCovered) {
        Set<Integer> uncovered;
        if (isNotInitPopulation(nature)) {
            uncovered = finalLastPopulationUnCovered;
        } else {
            uncovered = coverageTracker.getUncoveredLines(population.getTargetMethod());
        }
        return uncovered;
    }

    private static boolean isNotInitPopulation(Nature nature) {
        return !nature.getPopulationList().isEmpty();
    }

    /**
     * 获取染色体覆盖追踪器
     */
    public ChromosomeCoverageTracker getCoverageTracker() {
        return coverageTracker;
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
