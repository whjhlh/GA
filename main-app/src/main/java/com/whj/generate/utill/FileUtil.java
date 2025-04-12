package com.whj.generate.utill;

import com.whj.generate.core.domain.Chromosome;
import com.whj.generate.core.domain.GenePool;
import com.whj.generate.core.domain.Population;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @author whj
 * @date 2025-04-10 上午2:25
 */
public class FileUtil {
    /**
     * 生成报告
     *
     * @param initTime
     * @param population
     */
    public static void reportedInFile(long initTime, Population population) {
        if (null == population) {
            return;
        }
        GenePool genePool = population.getGenePool();
        List<Object[]> list = population.getChromosomes().stream().map(Chromosome::getGenes).toList();
        StringBuilder report = new StringBuilder(1024);
        //日期格式
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        report.append("=== 种群初始化报告 ===\n")
                .append(String.format("初始化时间：%s\n", dateFormat.format(date)))
                .append(String.format("耗时：%.3f ms\n", initTime / 1e6))
                .append("基因库概况：\n")
                .append(JsonUtil.toJson(genePool.getParameterGenes()))
                .append("\n")
                .append("理论生成染色体数：")
                .append(((int) Math.pow(genePool.getAverageGeneCount(), 0.5 * genePool.getParameterCount())+"\n"))
                .append("生成染色体数: ")
                .append(list.size())
                .append("\n")
                .append("=== 种群初始化结果 ===\n");
        for (Object[] genes : list) {
            report.append(JsonUtil.toJson(genes)).append("\n");
        }

// 写入文件代替控制台输出
        try {
            Files.writeString(Paths.get("population_report.txt"),
                    report.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
