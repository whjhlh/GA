package com.whj.generate.core.service.impl;

import com.whj.generate.biz.Infrastructure.JaCoCoCoverageAnalyzer;
import com.whj.generate.biz.Infrastructure.cache.ChromosomeCoverageTracker;
import com.whj.generate.core.domain.Chromosome;
import com.whj.generate.core.domain.GenePool;
import com.whj.generate.core.domain.Nature;
import com.whj.generate.core.domain.Population;
import com.whj.generate.core.exception.ExceptionWrapper;
import com.whj.generate.core.exception.GenerateErrorEnum;
import com.whj.generate.core.infrastructure.PoolLoader;
import com.whj.generate.core.service.CoverageService;
import com.whj.generate.core.service.FitnessCalculatorService;
import com.whj.generate.core.service.GenPoolService;
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
        // 遍历每个染色体，计算覆盖率并更新适应度
        Long totalCoverage = getTotalCoverage(nature, population);
        population.setCurrentCoverage(totalCoverage);
        population.updateFitnessCache();
        nature.addPopulation(population);
    }

    /**
     * 获取种群总覆盖
     *
     * @param nature
     * @param population
     * @return
     */
    private Long getTotalCoverage(Nature nature, Population population) {
        return ExceptionWrapper.process(() -> {
            // 初始化覆盖率追踪器（线程安全）
            final List<byte[]> coverageDataList = new ArrayList<>();
            // 获取每个染色体的覆盖率数据
            final Set<Chromosome> chromosomes = population.getChromosomeSet();
            chromosomes.forEach(chromosome -> {
                        // 处理单个染色体覆盖率数据
                        processChromosome(chromosome, coverageDataList);
                        fitnessCalculator.calculate(nature, population, chromosome);
                    }
            );
            return coverageAnalyzer.calculateTotalCoverage(coverageDataList, chromosomes.iterator().next().getMethod());
        }, GenerateErrorEnum.GET_OVERRIDE_FAIL, "种群覆盖率计算失败");
    }


    /**
     * 获取上次种群未覆盖行
     *
     * @param nature
     * @return
     */
    @Override
    public Set<Integer> getLastPopulationUncovered(Nature nature) {
        Set<Integer> lastPopulationUnCovered = Set.of();
        if (!nature.hasPopulation()) {
            //获取上个种群
            int size = nature.getPopulationList().size();
            final Population lastPopulation = nature.getPopulationList().get(size - 1);
            lastPopulationUnCovered = coverageTracker.getPopulationUnCoveredLines(lastPopulation);
        }
        return lastPopulationUnCovered;
    }

    /**
     * 获取指定种群未覆盖行
     */
    @Override
    public Set<Integer> getPopulationUncovered(Population population) {
        return coverageTracker.getPopulationUnCoveredLines(population);
    }


    /**
     * 处理单个染色体覆盖率数据
     */
    public void processChromosome(Chromosome chromosome, List<byte[]> dataCollector) {
        final Method method = chromosome.getMethod();
        final byte[] coverageData = getOrCollectCoverageData(chromosome, method);
        final double coverage = coverageAnalyzer.calculateCoveragePercentage(coverageData, chromosome);
        chromosome.setCoveragePercent((long) coverage);
        dataCollector.add(coverageData);
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
