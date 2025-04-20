package com.whj.generate.biz.Infrastructure.cache;

/**
 * @author whj
 * @date 2025-04-20 下午7:01
 */

import com.whj.generate.core.domain.Chromosome;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.IMethodCoverage;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 染色体适应度统计器，用于追踪每个染色体在方法和对应行上的覆盖情况。
 */
public class ChromosomeCoverageTracker {

    // 数据结构: Method -> 行号 -> Set<Chromosome>
    private final Map<Method, Map<Integer, Set<Chromosome>>> coverageMap = new HashMap<>();
    private final Map<Method, Map<Integer, Set<Chromosome>>> methodLineChromosomeMap = new HashMap<>();
    /** 染色体序列号Map **/
    private final Map<Chromosome, Integer> chromosomeSequenceMap = new HashMap<>();

    /**
     * 记录一个染色体覆盖了某个方法的某些行
     * @param chromosome
     * @param classCoverage
     */
    public void recordCoverageMapping(Chromosome chromosome, IClassCoverage classCoverage) {
        Method method = chromosome.getMethod();
        for (IMethodCoverage methodCoverage : classCoverage.getMethods()) {
            if (!methodCoverage.getName().equals(method.getName())) continue;
            for (int i = methodCoverage.getFirstLine(); i <= methodCoverage.getLastLine(); i++) {
                if (methodCoverage.getLine(i).getInstructionCounter().getCoveredCount() > 0) {
                    methodLineChromosomeMap
                            .computeIfAbsent(method, m -> new HashMap<>())
                            .computeIfAbsent(i, l -> new HashSet<>())
                            .add(chromosome);
                }
            }
        }
    }

    /**
     * 获取方法-行号-染色体映射
     * @return
     */
    public Map<Method, Map<Integer, Set<Chromosome>>> getMethodLineChromosomeMap() {
        return methodLineChromosomeMap;
    }
    /**
     * 记录一个染色体覆盖了某个方法的某些行
     * @param lineNumbers 覆盖的行号
     * @param chromosome 当前染色体
     */
    public void recordCoverage(List<Integer> lineNumbers, Chromosome chromosome) {
        Method method= chromosome.getMethod();
        Map<Integer, Set<Chromosome>> lineMap = coverageMap.computeIfAbsent(method, k -> new HashMap<>());
        for (Integer line : lineNumbers) {
            Set<Chromosome> chromosomes = lineMap.computeIfAbsent(line, k -> new HashSet<>());
            chromosomes.add(chromosome);
        }
    }

    /**
     * 获取某个方法某行的所有覆盖染色体
     */
    public Set<Chromosome> getChromosomesForLine(Method method, int line) {
        return Optional.ofNullable(coverageMap.get(method))
                .map(m -> m.getOrDefault(line, Collections.emptySet()))
                .orElse(Collections.emptySet());
    }

    /**
     * 获取整个结构
     */
    public Map<Method, Map<Integer, Set<Chromosome>>> getCoverageMap() {
        return coverageMap;
    }

    /**
     * 清空统计信息
     */
    public void clear() {
        coverageMap.clear();
    }
    /**
     * 生成可视化覆盖率报告（支持多级缩进格式）
     * @return 格式化后的覆盖率报告字符串
     */
    public String generateCoverageReport() {
        final StringBuilder report = new StringBuilder();
        final String lineSeparator = System.lineSeparator();

        // 输出方法-行号-染色体映射
        report.append("=== 行覆盖详情 ===").append(lineSeparator);
        methodLineChromosomeMap.forEach((method, lineMap) -> {
            report.append("方法: ").append(formatMethodSignature(method)).append(lineSeparator);
            lineMap.forEach((line, chromosomes) -> {
                report.append("  行数 ").append(line).append("  染色体数量")
                        .append(chromosomes.size()).append(lineSeparator);
                report.append("染色体(序列号)");
                chromosomes.forEach(ch -> report.append(formatChromosomeInfo(ch))
                );
                report.append(lineSeparator);
            });
            report.append(lineSeparator);
        });

        // 输出聚合统计
        report.append(lineSeparator).append("=== 详情 ===").append(lineSeparator);
        coverageMap.forEach((method, lineMap) -> {
            int totalLines = lineMap.size();
            long totalCoverage = lineMap.values().stream().mapToInt(Set::size).sum();
            report.append(formatMethodSignature(method))
                    .append(" | Covered Lines: ").append(totalLines)
                    .append(" | Total Coverage Count: ").append(totalCoverage)
                    .append(lineSeparator);
        });

        return report.toString();
    }

    public void buildChromosomeSequenceMap(Set<Chromosome> chromosomes) {
        for(Chromosome ch : chromosomes){
            if(!chromosomeSequenceMap.containsKey(ch)){
                chromosomeSequenceMap.put(ch, chromosomeSequenceMap.size()+1);
            }
        }
    }

    /**
     * 格式化方法签名（包含返回类型和参数）
     */
    private String formatMethodSignature(Method method) {
        return String.format("%s %s(%s)",
                method.getReturnType().getSimpleName(),
                method.getName(),
                Arrays.stream(method.getParameterTypes())
                        .map(Class::getSimpleName)
                        .collect(Collectors.joining(", "))
        );
    }

    /**
     * 格式化染色体信息（示例实现，可根据实际需求调整）
     */
    private String formatChromosomeInfo(Chromosome chromosome) {
        return String.format("%s,",chromosomeSequenceMap.get(chromosome));
    }

    public Map<Chromosome, Integer> getChromosomeSequenceMap() {
        return chromosomeSequenceMap;
    }
}
