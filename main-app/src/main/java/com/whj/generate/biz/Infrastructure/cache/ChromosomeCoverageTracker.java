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

/**
 * 染色体适应度统计器，用于追踪每个染色体在方法和对应行上的覆盖情况。
 */
public class ChromosomeCoverageTracker {

    // 数据结构: Method -> 行号 -> Set<Chromosome>
    private final Map<Method, Map<Integer, Set<Chromosome>>> coverageMap = new HashMap<>();
    private final Map<Method, Map<Integer, List<Chromosome>>> methodLineChromosomeMap = new HashMap<>();

    public void recordCoverageMapping(Chromosome chromosome, IClassCoverage classCoverage) {
        Method method = chromosome.getMethod();
        for (IMethodCoverage methodCoverage : classCoverage.getMethods()) {
            if (!methodCoverage.getName().equals(method.getName())) continue;
            for (int i = methodCoverage.getFirstLine(); i <= methodCoverage.getLastLine(); i++) {
                if (methodCoverage.getLine(i).getInstructionCounter().getCoveredCount() > 0) {
                    methodLineChromosomeMap
                            .computeIfAbsent(method, m -> new HashMap<>())
                            .computeIfAbsent(i, l -> new ArrayList<>())
                            .add(chromosome);
                }
            }
        }
    }

    public Map<Method, Map<Integer, List<Chromosome>>> getMethodLineChromosomeMap() {
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
}
