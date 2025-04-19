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
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.IMethodCoverage;
import org.jacoco.core.data.ExecutionDataReader;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.springframework.util.CollectionUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * @author whj
 * @date 2025-04-18 上午1:17
 */
public class JaCoCoCoverageAnalyzer {
    /**
     * 获取覆盖
     *
     * @param population 种群
     * @return
     */
    public static void getCoverageWithPopulation(Nature nature, Population population) {
        // 获取JaCoCo Agent实例
        Set<Chromosome> chromosomeSet = population.getChromosomeSet();

        List<byte[]> populationCoverageList = new ArrayList<>();
        Long coveredWithPopulation = ExceptionWrapper.process(() -> {
            Method method = chromosomeSet.iterator().next().getMethod();
            for (Chromosome chromosome : chromosomeSet) {
                byte[] ChromosomeCoverageData = nature.getChromosomeCoverageDataMap().get(chromosome);
                if (ChromosomeCoverageData == null) {
                    ChromosomeCoverageData = collectCoverageDataByChromosome(chromosome, nature, method);
                    double ChromosomeCoverageDataFitness = getLineCoverageFromData(ChromosomeCoverageData, method);
                    chromosome.setFitness((long) ChromosomeCoverageDataFitness);
                    nature.getChromosomeCoverageDataMap().put(chromosome, ChromosomeCoverageData);
                    nature.getMethodCoverageDataSet().add(ChromosomeCoverageData);
                }
                populationCoverageList.add(ChromosomeCoverageData);
            }
            double totalCoverage = getTotalLineCoverage(populationCoverageList, method);
            return (long) totalCoverage;
        }, GenerateErrorEnum.GET_OVERRIDE_FAIL, "获取覆盖失败 ");
        population.setCurrentCoverage(coveredWithPopulation);
    }

    private static byte[] collectCoverageDataByChromosome(Chromosome chromosome, Nature nature, Method method) {
        return ExceptionWrapper.process(new Callable<byte[]>() {
            @Override
            public byte[] call() throws Exception {
                // 重置覆盖率数据，确保每次调用独立统计
                IAgent agent = nature.getAgent();
                agent.reset();
                Object instance = method.getDeclaringClass().getDeclaredConstructor().newInstance();
                ReflectionUtil.invokeSafe(method, chromosome.getGenes(), instance);
                // 获取当前数据且不重置
                return agent.getExecutionData(false);
            }
        }, GenerateErrorEnum.COLLECT_COVERAGE_FAIL, ",染色体收集行覆盖失败");
    }

    /**
     * 获取行覆盖
     *
     * @param data
     * @param method
     * @return
     */
    public static double getLineCoverageFromData(byte[] data, Method method) {
        Double process = ExceptionWrapper.process(() -> {
            ExecutionDataStore store = new ExecutionDataStore();
            SessionInfoStore sessionStore = new SessionInfoStore();
            ExecutionDataReader reader = new ExecutionDataReader(new ByteArrayInputStream(data));
            reader.setExecutionDataVisitor(store);
            reader.setSessionInfoVisitor(sessionStore);
            reader.read();
            return analyzeCoverage(store, method);
        }, GenerateErrorEnum.GET_OVERRIDE_FAIL, "获取覆盖失败 %s", method.getName());
        return process;
    }

    /**
     * 获取总覆盖
     *
     * @param populationCoverageList
     * @param method
     * @return
     */
    public static double getTotalLineCoverage(List<byte[]> populationCoverageList, Method method) {
        if (CollectionUtils.isEmpty(populationCoverageList)) {
            return 0L;
        }
        Double process = ExceptionWrapper.process(() -> {
            SessionInfoStore sessionStore = new SessionInfoStore();
            ExecutionDataStore totalStore = new ExecutionDataStore();
            for (byte[] data : populationCoverageList) {
                ExecutionDataReader reader = new ExecutionDataReader(new ByteArrayInputStream(data));
                reader.setExecutionDataVisitor(totalStore);
                reader.setSessionInfoVisitor(sessionStore);
                reader.read();
            }
            return analyzeCoverage(totalStore, method);
        }, GenerateErrorEnum.GET_OVERRIDE_FAIL, "获取总覆盖失败 %s", method.getName());
        return process;
    }

    private static double analyzeCoverage(ExecutionDataStore store, Method method) throws Exception {
        try (InputStream classStream = method.getDeclaringClass()
                .getClassLoader()
                .getResourceAsStream(method.getDeclaringClass().getName().replace('.', '/') + ".class")) {
            CoverageBuilder coverageBuilder = new CoverageBuilder();
            new Analyzer(store, coverageBuilder).analyzeClass(classStream, method.getDeclaringClass().getName());

            for (IClassCoverage cc : coverageBuilder.getClasses()) {
                for (IMethodCoverage mc : cc.getMethods()) {
                    if (mc.getName().equals(method.getName()) &&
                            mc.getDesc().equals(Type.getMethodDescriptor(method))) {
                        int covered = mc.getLineCounter().getCoveredCount();
                        int total = mc.getLineCounter().getTotalCount();
                        return total == 0 ? 0.0 : (double) covered / total * 100;
                    }
                }
            }
            return 0.0;
        }
    }

    /**
     * 计算适应度
     *
     * @param child
     * @param nature
     */
    public static long calculateFitness(Nature nature, Chromosome child) {
        Method method = child.getMethod();
        byte[] chromosomeCoverageData = collectCoverageDataByChromosome(child, nature, method);
        double currentCoverage = getLineCoverageFromData(chromosomeCoverageData, method);
        nature.getChromosomeCoverageDataMap().put(child, chromosomeCoverageData);
        return (long) currentCoverage;
    }
}