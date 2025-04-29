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

    // Method -> 行号 -> Set<Chromosome>
    private final Map<Method, Map<Integer, Set<Chromosome>>> coverageMap = new HashMap<>();
    /**
     * 染色体序列号Map
     **/
    private final Map<Chromosome, Integer> chromosomeSequenceMap = new HashMap<>();
    /**
     * 起始行
     */
    private Integer startLine;
    /**
     * 结束行
     */
    private Integer endLine;

    // 私有工具方法
    private static <T> Set<T> intersection(Set<T> a, Set<T> b) {
        return a.stream().filter(b::contains).collect(Collectors.toSet());
    }

    /**
     * 初始化起始行和结束行
     * @param start
     * @param end
     */
    public void init(Integer start, Integer end) {
        if (startLine == null) {
            this.startLine = start;
        }
        if (endLine == null) {
            this.endLine = end;
        }
    }

    /**
     * 获取新覆盖的行号（对比当前未覆盖行）
     *
     * @param chromosome       目标染色体
     * @param currentUncovered 当前未覆盖行集合
     * @return 该染色体覆盖的新行号集合
     */
    public Set<Integer> getNewLinesCovered(Chromosome chromosome, Set<Integer> currentUncovered) {
        Set<Integer> covered = getLinesCoveredBy(chromosome);
        return intersection(covered, currentUncovered);
    }

    /**
     * 获取方法的当前未覆盖行
     */
    public Set<Integer> getUncoveredLines(Method method) {
        Map<Integer, Set<Chromosome>> methodCoverage = getMethodCoverage(method);
        Set<Integer> currentUncovered = new HashSet<>();
        Set<Integer> currentCovered = methodCoverage.keySet();
        for (int i = startLine; i <= endLine; i++) {
            if (!currentCovered.contains(i)) {
                currentUncovered.add(i);
            }
        }
        return currentUncovered;
    }

    /**
     * 计算两个染色体的Jaccard相似度
     */
    public double calculateJaccardSimilarity(Chromosome c1, Chromosome c2) {
        if (c1.getMethod() != c2.getMethod()) return 0.0;

        Set<Integer> lines1 = getLinesCoveredBy(c1);
        Set<Integer> lines2 = getLinesCoveredBy(c2);
        if (lines1.isEmpty() && lines2.isEmpty()) return 0.0;

        int intersection = intersection(lines1, lines2).size();
        int union = lines1.size() + lines2.size() - intersection;
        return (double) intersection / union;
    }

    /**
     * 根据方法名获取方法覆盖率数据
     *
     * @param coverage
     * @param methodName
     * @return
     */
    private IMethodCoverage findMethodCoverage(IClassCoverage coverage, String methodName) {
        return coverage.getMethods().stream()
                .filter(m -> m.getName().equals(methodName))
                .findFirst()
                .orElse(null);
    }

    /**
     * 获取某个染色体覆盖的行
     *
     * @param chromosome
     * @return
     */
    public Set<Integer> getLinesCoveredBy(Chromosome chromosome) {
        Map<Integer, Set<Chromosome>> methodCoverage = getMethodCoverage(chromosome);
        //如果集合中存在该行，则返回该行被哪些染色体覆盖
        if (methodCoverage == null) {
            return Collections.emptySet();
        }
        return Collections.singleton(methodCoverage.entrySet().stream()
                .filter(entry -> entry.getValue().contains(chromosome))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null));
    }

    /**
     * 获取某个方法某行被哪些染色体覆盖
     *
     * @param method
     * @param line
     * @return
     */
    public Set<Chromosome> getCoveringChromosomes(Method method, int line) {
        Map<Integer, Set<Chromosome>> methodCoverage = getMethodCoverage(method);
        if (methodCoverage == null) {
            return Collections.emptySet();
        }
        return methodCoverage.getOrDefault(line, Collections.emptySet());
    }


    /**
     * 记录一个染色体覆盖了某个方法的某些行
     *
     * @param lineNumbers 覆盖的行号
     * @param chromosome  当前染色体
     */
    public void recordCoverage(List<Integer> lineNumbers, Chromosome chromosome) {
        Method method = chromosome.getMethod();
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
     * 获取某个方法覆盖情况
     */
    public Map<Integer, Set<Chromosome>> getMethodCoverage(Method method) {
        return coverageMap.get(method);
    }

    /**
     * 获取某个染色体覆盖情况
     */
    public Map<Integer, Set<Chromosome>> getMethodCoverage(Chromosome chromosome) {
        Method method = chromosome.getMethod();
        return coverageMap.get(method);
    }

    /**
     * 清空统计信息
     */
    public void clear() {
        coverageMap.clear();
    }

    /**
     * 生成可视化覆盖率报告（支持多级缩进格式）
     *
     * @return 格式化后的覆盖率报告字符串
     */
    public String generateCoverageReport() {
        final StringBuilder report = new StringBuilder();
        final String lineSeparator = System.lineSeparator();

        // 输出方法-行号-染色体映射
        report.append("=== 行覆盖详情 ===").append(lineSeparator);
        coverageMap.forEach((method, lineMap) -> {
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


    public Set<Chromosome> getCoveringChromosomeSet(Chromosome target) {

        Method method = target.getMethod();
        Map<Integer, Set<Chromosome>> methodCoverage = getMethodCoverage(method);
        Set<Chromosome> coveringChromosomes = new HashSet<>();
        //遍历 map
        for (Map.Entry<Integer, Set<Chromosome>> entry : methodCoverage.entrySet()) {
            int line = entry.getKey();
            coveringChromosomes.addAll(getChromosomesForLine(method, line));
        }
        return coveringChromosomes;
    }

    /**
     * 构建染色体序列号映射
     *
     * @param chromosomes
     */
    public void buildChromosomeSequenceMap(Set<Chromosome> chromosomes) {
        for (Chromosome ch : chromosomes) {
            if (!chromosomeSequenceMap.containsKey(ch)) {
                chromosomeSequenceMap.put(ch, chromosomeSequenceMap.size() + 1);
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
        return String.format("%s,", chromosomeSequenceMap.get(chromosome));
    }

    public Map<Chromosome, Integer> getChromosomeSequenceMap() {
        return chromosomeSequenceMap;
    }
}
