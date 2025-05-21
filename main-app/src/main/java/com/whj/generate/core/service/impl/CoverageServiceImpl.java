package com.whj.generate.core.service.impl;

import com.whj.generate.biz.Infrastructure.JaCoCoCoverageAnalyzer;
import com.whj.generate.biz.Infrastructure.cache.ChromosomeCoverageTracker;
import com.whj.generate.common.service.JavaStructureService;
import com.whj.generate.core.domain.Chromosome;
import com.whj.generate.core.domain.Covered;
import com.whj.generate.core.domain.Nature;
import com.whj.generate.core.domain.Population;
import com.whj.generate.core.exception.ExceptionWrapper;
import com.whj.generate.core.exception.GenerateErrorEnum;
import com.whj.generate.core.service.CoverageService;
import com.whj.generate.utill.SetUtils;
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
    private final FitnessCalculatorServiceImpl fitnessCalculator;
    /**
     * java结构服务
     */
    private final JavaStructureService javaStructureService;

    @Autowired
    public CoverageServiceImpl(JaCoCoCoverageAnalyzer coverageAnalyzer, ChromosomeCoverageTracker coverageTracker, FitnessCalculatorServiceImpl fitnessCalculator, JavaStructureService javaStructureService) {
        this.coverageAnalyzer = coverageAnalyzer;
        this.coverageTracker = coverageTracker;
        this.fitnessCalculator = fitnessCalculator;
        this.javaStructureService = javaStructureService;
    }

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

    @Override
    public Set<Integer> getPopulationUncovered(Population population) {
        return coverageTracker.getPopulationUnCoveredLines(population);
    }

    @Override
    public Covered getCovered(Integer chromosomeSeq) {
        //查询value为chromosomeSeq的Integer
        Chromosome chromosome = coverageTracker.getChromosomeById(chromosomeSeq);
        Covered covered = new Covered();
        if(chromosome!=null){
            String className = chromosome.getTargetClass().getSimpleName();
            String methodName = chromosome.getMethod().getName();

            Set<Integer> chromosomeCovered = coverageTracker.getChromosomeCovered(chromosome);
            Set<Integer> chromosomeUnCovered = coverageTracker.getChromosomeUnCovered(chromosome);

            Set<Integer> cantNotCoveredLines = javaStructureService.cantNotCoveredLines(className, methodName);
            chromosomeCovered=SetUtils.leftDifference(chromosomeCovered,cantNotCoveredLines);
            chromosomeUnCovered=SetUtils.leftDifference(chromosomeUnCovered,cantNotCoveredLines);

            covered.setCoveredLine(chromosomeCovered);
            covered.setUnCoveredLine(chromosomeUnCovered);
            covered.setGenes(chromosome.getGenes());
            covered.setChromosomeId(String.valueOf(chromosomeSeq));
        }
        return covered;
    }

    @Override
    public Chromosome getChromosomeById(Integer chromosomeId) {
        return coverageTracker.getChromosomeById(chromosomeId);
    }

    @Override
    public Map<Chromosome, Integer> getChromosomeSequenceMap() {
        return coverageTracker.getChromosomeSequenceMap();
    }



    /**
     * 处理单个染色体覆盖率数据
     */
    public void processChromosome(Chromosome chromosome, List<byte[]> dataCollector) {

        final byte[] coverageData = getOrCollectCoverageData(chromosome);
        final double coverage = coverageAnalyzer.calculateCoveragePercentage(coverageData, chromosome);
        chromosome.setCoveragePercent((long) coverage);
        dataCollector.add(coverageData);
    }

    /**
     * 获取或收集染色体覆盖率数据
     */
    public byte[] getOrCollectCoverageData(Chromosome chromosome) {
        Map<Chromosome, byte[]> chromosomeCoverageDataMap = coverageTracker.getChromosomeCoverageDataMap();
        // naturemap中获取或计算覆盖率数据
        return Optional.ofNullable(chromosomeCoverageDataMap)
                .map(map -> map.computeIfAbsent(chromosome,
                        k -> coverageAnalyzer.collectNewCoverageData(chromosome)))
                .orElseGet(
                        () -> coverageAnalyzer.collectNewCoverageData(chromosome));
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

}
