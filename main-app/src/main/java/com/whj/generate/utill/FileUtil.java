package com.whj.generate.utill;

import com.whj.generate.biz.Infrastructure.cache.ChromosomeCoverageTracker;
import com.whj.generate.core.domain.Chromosome;
import com.whj.generate.core.domain.GenePool;
import com.whj.generate.core.domain.Population;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

/**
 * @author whj
 * @date 2025-04-10 上午
 */
public class FileUtil {
    // 配置报告存放目录（根据需求修改）
    private static final String REPORT_DIR = "/Users/b20210304129/Desktop/school/biyesheji/generatetestclass/report/";

    /**
     * 生成报告到指定目录
     *
     * @param initTime        初始化耗时(ns)
     * @param population      种群数据
     * @param desc            报告描述（用于文件名）
     * @param coverageTracker
     */
    public static void reportedInFile(long initTime, Population population, String desc, ChromosomeCoverageTracker coverageTracker) {
        if (null == population) {
            return;
        }

        try {
            // 确保目录存在
            Files.createDirectories(Paths.get(REPORT_DIR));
        } catch (IOException e) {
            throw new RuntimeException("创建报告目录失败: " + REPORT_DIR, e);
        }

        GenePool genePool = population.getGenePool();
        Set<Chromosome> chromosomes = population.getChromosomeSet();
        // 构建染色体序列
        coverageTracker.buildChromosomeSequenceMap(chromosomes);

        // 构建报告内容
        String reportContent = buildReportContent(initTime, population, genePool, chromosomes, coverageTracker);

        // 生成文件名
        String fileName = String.format("population_report_%s.txt",
                desc);

        // 写入文件
        writeToFile(reportContent, fileName);
    }

    private static String buildReportContent(long initTime,
                                             Population population,
                                             GenePool genePool,
                                             Set<Chromosome> chromosomes, ChromosomeCoverageTracker coverageTracker) {
        String str = coverageTracker.generateCoverageReport();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        StringBuilder report = new StringBuilder(1024)
                .append("=== 种群报告").append(population.getTargetMethod().getName()).append(" ===\n")
                .append(String.format("报告时间：%s\n", dateFormat.format(new Date())))
                .append(String.format("执行耗时：%.3f ms\n", initTime / 1e6))
                .append("基因库参数数量：").append(genePool.getParameterCount()).append("\n")
                .append("基因分布：\n").append(JsonUtil.toJson(genePool.getParameterGenes())).append("\n")
                .append("理论染色体组合数：")
                .append((int) Math.pow(genePool.getAverageGeneCount(), 0.5 * genePool.getParameterCount())).append("\n")
                .append("实际生成数：").append(chromosomes.size()).append("\n")
                .append("=== 染色体详情 ===\n");

        chromosomes.stream()
                .sorted((c1, c2) -> Double.compare(c2.getCoveragePercent(), c1.getCoveragePercent()))
                .forEach(c -> report.append(coverageTracker.getChromosomeSequenceMap().get(c)).append(JsonUtil.toJson(c.getGenes()))
                        .append(" - 覆盖率").append(c.getCoveragePercent()).append("%\n"));

        report.append("=== 汇总 ===\n")
                .append("总覆盖率：").append(population.getCurrentCoverage()).append("%\n");
        report.append(str);
        return report.toString();
    }

    /**
     * 写入文件
     *
     * @param content
     * @param fileName
     */
    private static void writeToFile(String content, String fileName) {
        Path filePath = Paths.get(REPORT_DIR, fileName);
        try {
            Files.writeString(filePath, content);
            System.out.println("报告已生成至: " + filePath.toAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("写入报告文件失败: " + filePath, e);
        }
    }
}
