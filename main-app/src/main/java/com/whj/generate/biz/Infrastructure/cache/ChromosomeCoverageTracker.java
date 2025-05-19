package com.whj.generate.biz.Infrastructure.cache;

/**
 * @author whj
 * @date 2025-04-20 下午7:01
 */

import com.google.common.collect.Lists;
import com.whj.generate.core.domain.Chromosome;
import com.whj.generate.core.domain.Nature;
import com.whj.generate.core.domain.Population;
import com.whj.generate.utill.SetUtils;
import com.whj.generate.utill.SimilarityUtils;
import org.jacoco.agent.rt.internal_aeaf9ab.asm.Type;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ILine;
import org.jacoco.core.analysis.IMethodCoverage;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 染色体适应度统计器，用于追踪每个染色体在方法和对应行上的覆盖情况。
 */
public class ChromosomeCoverageTracker {

    // Method -> 行号 -> Set<Chromosome>
    private static final Map<Method, Map<Integer, Set<Chromosome>>> coverageMap = new HashMap<>();
    /**
     * 染色体序列号Map
     **/
    private final Map<Chromosome, Integer> chromosomeSequenceMap = new HashMap<>();
    /**
     * 起始行
     */
    private static Integer startLine;
    /**
     * 结束行
     */
    private static Integer endLine;

    private Map<Chromosome, byte[]> chromosomeCoverageDataMap = new HashMap<>();

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
            startLine = start;
        }
        if (endLine == null) {
            endLine = end;
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
        Set<Integer> covered = getChromosomeCovered(chromosome);
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
        // 依赖 getChromosomeCovered(chromosome) 返回 Set<Integer>
        population.getChromosomeSet().stream()
                .flatMapToInt(chromosome -> getChromosomeCovered(chromosome).stream().mapToInt(Integer::intValue))
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
    public Set<Integer> getChromosomeCovered(Chromosome chromosome) {
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
     * 获取某个染色体覆盖的行
     *
     * @param chromosome
     * @return
     */
    public Set<Integer> getChromosomeUnCovered(Chromosome chromosome) {
        Map<Integer, Set<Chromosome>> methodCoverage = getMethodCoverage(chromosome);
        //如果集合中存在该行，则返回该行被哪些染色体覆盖
        if (methodCoverage == null) {
            return Collections.emptySet();
        }
        Set<Integer> lines = new HashSet<>();
        for (Integer key : methodCoverage.keySet()) {
            if (!methodCoverage.get(key).contains(chromosome)) {
                lines.add(key);
            }
        }
        return lines;
    }

    /**
     * 根据chromosomeSeq获取chromosome
     * @param chromosomeSeq
     * @return
     */
    public   Chromosome getChromosomeById(Integer chromosomeSeq) {
        Chromosome chromosome = null;
        for (Map.Entry<Chromosome, Integer> entry : chromosomeSequenceMap.entrySet()) {
            if (Objects.equals(entry.getValue(), chromosomeSeq)) {
                chromosome=entry.getKey();
            }
        }
        return chromosome;
    }

    /**
     * 记录一个染色体覆盖了某个方法的某些行
     *
     * @param lineNumbers 覆盖的行号
     * @param chromosome  当前染色体
     */
    public void recordCoverage(List<Integer> lineNumbers, Chromosome chromosome) {
        Method method =chromosome.getMethod();
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
    public static Map<Integer, Set<Chromosome>> getMethodCoverage(Method method) {
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
     * 覆盖记录
     *
     * @param classCoverage
     * @param method
     * @param chromosome
     */
    public void processClassCoverage(IClassCoverage classCoverage, Method method, Chromosome chromosome) {
        // 记录每个覆盖行与染色体的映射关系
        List<Integer> coveredLines = new ArrayList<>();

        classCoverage.getMethods().stream()
                .filter(mc -> isTargetMethod(mc, method))
                .forEach(mc -> {
                    init(mc.getFirstLine(), mc.getLastLine());
                    for (int i = mc.getFirstLine(); i <= mc.getLastLine(); i++) {
                        ILine line = classCoverage.getLine(i);
                        if (line != null) {
                            if (line.getStatus() == ICounter.FULLY_COVERED || line.getStatus() == ICounter.PARTLY_COVERED) {
                                coveredLines.add(i);
                            }
                        }
                    }
                });
        // 调用追踪器的记录数据方法
        recordCoverage(coveredLines, chromosome);
    }

    /**
     * 判断是否为目标方法
     */
    private static boolean isTargetMethod(IMethodCoverage mc, Method method) {
        return mc.getName().equals(method.getName())
                && mc.getDesc().equals(Type.getMethodDescriptor(method));
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
                .map(this::getChromosomeCovered)
                .collect(Collectors.toList());
        // 调用工具类计算平均相似度
        return SimilarityUtils.averagePopulationSimilarity(linesList);
    }

    public static Integer getStartLine() {
        return startLine;
    }

    public static void setStartLine(Integer startLine) {
        ChromosomeCoverageTracker.startLine = startLine;
    }

    public static Integer getEndLine() {
        return endLine;
    }

    public static void setEndLine(Integer endLine) {
        ChromosomeCoverageTracker.endLine = endLine;
    }

    public Map<Chromosome, byte[]> getChromosomeCoverageDataMap() {
        return chromosomeCoverageDataMap;
    }

    public void setChromosomeCoverageDataMap(Map<Chromosome, byte[]> chromosomeCoverageDataMap) {
        this.chromosomeCoverageDataMap = chromosomeCoverageDataMap;
    }

    /**
     * 获取染色体新覆盖行
     * @param nature
     * @param population
     * @param chromosome
     * @return
     */
    public Set<Integer> getNewCovered(Nature nature, Population population, Chromosome chromosome) {
        Set<Integer> uncovered = getUncovered(nature, population);
        Set<Integer> linesCoveredBy = getChromosomeCovered(chromosome);
        return SetUtils.intersection(uncovered, linesCoveredBy);
    }

    /**
     * 获取未覆盖行<br/>
     * 首先获取环境中上一代，若上代没有，则获取当前种群未覆盖行
     *
     * @param nature
     * @param population
     * @return
     */
    public Set<Integer> getUncovered(Nature nature, Population population) {
        Set<Integer> uncovered = getLastPopulationUnCovered(nature);
        if (uncovered.isEmpty()) {
            uncovered = getPopulationUnCoveredLines(population);
        }
        return uncovered;
    }

    /**
     * 获取上次种群未覆盖行
     *
     * @param nature
     * @return
     */
    public Set<Integer> getLastPopulationUnCovered(Nature nature) {
        Set<Integer> lastPopulationUnCovered = Set.of();
        if (!nature.hasPopulation()) {
            //获取上个种群
            int size = nature.getPopulationList().size();
            final Population lastPopulation = nature.getPopulationList().get(size - 1);
            lastPopulationUnCovered = getPopulationUnCoveredLines(lastPopulation);
        }
        return lastPopulationUnCovered;
    }
}
