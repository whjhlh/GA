package com.whj.generate.biz.Infrastructure.cache;

/**
 * @author whj
 * @date 2025-04-20 下午7:01
 */

import com.google.common.collect.Lists;
import com.whj.generate.core.domain.Chromosome;
import com.whj.generate.core.domain.Population;
import com.whj.generate.utill.SimilarityUtils;

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
     *
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
     * 获取所有种群（所有染色体）的方法的当前未覆盖行
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
     * 构建一个 BitSet：位序号 0 对应 startLine，1 对应 startLine+1，以此类推，
     * 被覆盖的行在 BitSet 中对应 bit 会被置为 true。
     */
    public BitSet buildCoverageBitSet(Population population) {
        int length = endLine - startLine + 1;
        BitSet bitSet = new BitSet(length);
        // 依赖 getLinesCoveredBy(chromosome) 返回 Set<Integer>
        population.getChromosomeSet().stream()
                .flatMapToInt(chromosome -> getLinesCoveredBy(chromosome).stream().mapToInt(Integer::intValue))
                .filter(line -> line >= startLine && line <= endLine)
                .map(line -> line - startLine)
                .forEach(bitSet::set);
        return bitSet;
    }

    /**
     * 获取种群已覆盖的行号集合
     */
    public Set<Integer> getPopulationCoveredLines(Population population) {
        BitSet bitSet = buildCoverageBitSet(population);
        return bitSet.stream()
                .mapToObj(idx -> idx + startLine)
                .collect(Collectors.toSet());
    }

    /**
     * 获取种群未覆盖的行号集合
     */
    public Set<Integer> getPopulationUnCoveredLines(Population population) {
        BitSet bitSet = buildCoverageBitSet(population);
        bitSet.flip(0, endLine - startLine + 1);  // 翻转后，true 表示未覆盖
        return bitSet.stream()
                .mapToObj(idx -> idx + startLine)
                .collect(Collectors.toSet());
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
        Set<Integer> lines = new HashSet<>();
        for (Integer key : methodCoverage.keySet()) {
            if (methodCoverage.get(key).contains(chromosome)) {
                lines.add(key);
            }
        }
        return lines;
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
    public Set<Chromosome> getChromosomesForLine(Chromosome target, int line) {
        Method method = target.getMethod();
        return Optional.ofNullable(coverageMap.get(method))
                .map(m -> m.getOrDefault(line, Collections.emptySet()))
                .orElse(Collections.emptySet());
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
        chromosomeSequenceMap.clear();
    }

    /**
     * 获取覆盖该染色体的所有染色体
     *
     * @param target
     * @return
     */
    public Set<Chromosome> getCoveringChromosomeSet(Chromosome target) {
        //获取目标染色体覆盖的行
        Map<Integer, Set<Chromosome>> methodCoverage = getMethodCoverage(target);
        Set<Chromosome> coveringChromosomes = new HashSet<>();
        //遍历 map
        for (Map.Entry<Integer, Set<Chromosome>> entry : methodCoverage.entrySet()) {
            int line = entry.getKey();
            coveringChromosomes.addAll(getChromosomesForLine(target, line));
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

    public Map<Chromosome, Integer> getChromosomeSequenceMap() {
        return chromosomeSequenceMap;
    }

    /**
     * 获取种群相似度
     *
     * @param pop
     * @return
     */
    public double getSimilarityAtGeneration(Population pop) {
        List<Chromosome> chromosomes = Lists.newArrayList(pop.getChromosomeSet());
        // 收集每个染色体的覆盖行集合
        List<Set<Integer>> linesList = chromosomes.stream()
                .map(this::getLinesCoveredBy)
                .collect(Collectors.toList());
        // 调用工具类计算平均相似度
        return SimilarityUtils.averagePopulationSimilarity(linesList);
    }


}
