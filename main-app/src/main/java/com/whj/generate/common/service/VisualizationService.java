package com.whj.generate.common.service;

import com.whj.generate.core.domain.Chromosome;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.SortedSet;

/**
 * @author whj
 * @date 2025-05-15 下午4:28
 */
public interface VisualizationService {

    // region 基础统计接口
    /**
     * 记录染色体覆盖行信息
     * @param lineNumbers 覆盖行号集合
     * @param chromosome 目标染色体
     */
    void recordCoverage(Collection<Integer> lineNumbers, Chromosome chromosome);

    /**
     * 获取染色体覆盖行集合
     * @param chromosome 目标染色体
     * @return 有序行号集合（按行号排序）
     */
    SortedSet<Integer> getCoveredLines(Chromosome chromosome);

    /**
     * 获取方法覆盖热力图数据
     * @param method 目标方法
     * @return 行号 -> 覆盖次数映射
     */
    Map<Integer, Integer> getMethodHeatmapData(Method method);
    // endregion

    // region 高级分析接口
    /**
     * 获取代际覆盖趋势数据
     * @param generationRange 代数范围 [startGen, endGen]
     * @return 按代分组的覆盖率数据
     */
    Map<Integer, Double> getCoverageTrend(int[] generationRange);

    /**
     * 获取参数-适应度关联数据
     * @param paramName 参数名称（如"crossoverRate"）
     * @return 参数值 -> 平均适应度
     */
    Map<Object, Double> getParamFitnessCorrelation(String paramName);
    // endregion

    // region 可视化配置接口
    /**
     * 设置可视化范围
     * @param method 目标方法
     * @param startLine 起始行（包含）
     * @param endLine 结束行（包含）
     */
    void setCodeScope(Method method, int startLine, int endLine);

    /**
     * 获取当前代码范围
     * @return [startLine, endLine]
     */
    int[] getCurrentCodeScope();
    // endregion
}