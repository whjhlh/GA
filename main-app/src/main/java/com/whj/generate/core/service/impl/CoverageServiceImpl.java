package com.whj.generate.core.service.impl;

import com.whj.generate.biz.Infrastructure.JaCoCoCoverageAnalyzer;
import com.whj.generate.biz.Infrastructure.cache.ChromosomeCoverageTracker;
import com.whj.generate.core.domain.Chromosome;
import com.whj.generate.core.domain.Nature;
import com.whj.generate.core.domain.Population;
import com.whj.generate.core.exception.ExceptionWrapper;
import com.whj.generate.core.exception.GenerateErrorEnum;
import com.whj.generate.core.service.CoverageService;
import com.whj.generate.core.service.FitnessCalculatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.*;

/**
 * @author whj
 * @date 2025-05-19 下午12:50
 */
@Service
public class CoverageServiceImpl implements CoverageService {
    /**
     * 覆盖分析
     */
    private final JaCoCoCoverageAnalyzer coverageAnalyzer;
    /**
     * 业务数据统计-覆盖追踪
     */
    private final ChromosomeCoverageTracker coverageTracker;
    /**
     * 适应度计算服务
     */
    private final FitnessCalculatorService fitnessCalculator;

    @Autowired
    public CoverageServiceImpl(JaCoCoCoverageAnalyzer coverageAnalyzer, ChromosomeCoverageTracker coverageTracker, FitnessCalculatorService fitnessCalculator) {
        this.coverageAnalyzer = coverageAnalyzer;
        this.coverageTracker = coverageTracker;
        this.fitnessCalculator = fitnessCalculator;
    }

    /**
     * 计算种群覆盖率并更新适应度
     *
     * @param nature     环境参数
     * @param population 种群对象
     */
    @Override
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
                        fitnessCalculator.calculate(chromosome, uncovered);
                    }
            );
            return coverageAnalyzer.calculateTotalCoverage(coverageDataList, chromosomes.iterator().next().getMethod());
        }, GenerateErrorEnum.GET_OVERRIDE_FAIL, "种群覆盖率计算失败");
        population.setCurrentCoverage(totalCoverage);
        population.updateFitnessCache();

        nature.addPopulation(population);
    }

    /**
     * 获取上次种群未覆盖行
     *
     * @param nature
     * @return
     */
    public Set<Integer> getLastPopulationUnCovered(Nature nature) {
        Set<Integer> lastPopulationUnCovered = Set.of();
        if (!nature.isInitPopulation()) {
            //获取上个种群
            int size = nature.getPopulationList().size();
            final Population lastPopulation = nature.getPopulationList().get(size - 1);
            lastPopulationUnCovered = coverageTracker.getPopulationUnCoveredLines(lastPopulation);
        }
        return lastPopulationUnCovered;
    }

    /**
     * 获取种群未覆盖
     *
     * @param nature
     * @param population
     * @param finalLastPopulationUnCovered
     * @return
     */
    public Set<Integer> getUncovered(Nature nature, Population population, Set<Integer> finalLastPopulationUnCovered) {
        Set<Integer> uncovered;
        if (nature.isInitPopulation()) {
            uncovered = finalLastPopulationUnCovered;
        } else {
            uncovered = coverageTracker.getUncoveredLines(population.getTargetMethod());
        }
        return uncovered;
    }


    /**
     * 处理单个染色体覆盖率数据
     */
    public void processChromosome(Nature nature, Chromosome chromosome, List<byte[]> dataCollector) {
        final Method method = chromosome.getMethod();
        final byte[] coverageData = getOrCollectCoverageData(chromosome, method);
        chromosome.setCoveragePercent(calculateChromosomePercentage(nature, chromosome));
        dataCollector.add(coverageData);
    }


    /**
     * 计算染色体适应度（新增追踪逻辑）
     */
    public long calculateChromosomePercentage(Nature nature, Chromosome chromosome) {
        final Method method = chromosome.getMethod();
        final byte[] data = getOrCollectCoverageData(chromosome, method);
        final double coverage = coverageAnalyzer.calculateCoveragePercentage(data, chromosome);
        return (long) coverage;
    }

    /**
     * 获取或收集染色体覆盖率数据
     */
    public byte[] getOrCollectCoverageData(Chromosome chromosome, Method method) {
        Map<Chromosome, byte[]> chromosomeCoverageDataMap = coverageTracker.getChromosomeCoverageDataMap();
        // naturemap中获取或计算覆盖率数据
        return Optional.ofNullable(chromosomeCoverageDataMap)
                .map(map -> map.computeIfAbsent(chromosome,
                        k -> coverageAnalyzer.collectNewCoverageData(method, chromosome.getGenes())))
                .orElseGet(
                        () -> coverageAnalyzer.collectNewCoverageData(method, chromosome.getGenes()));
    }

}
