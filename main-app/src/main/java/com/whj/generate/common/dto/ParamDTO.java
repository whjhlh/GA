package com.whj.generate.common.dto;

import com.whj.generate.common.config.GeneticAlgorithmConfig;

/**
 * @author whj
 * @date 2025-05-04 下午10:12
 */
public class ParamDTO {
    private static long VERSION = 0L;
    /**
     * 版本号
     */
    private long configVersion;
    private String initPhase;

    /*--进化操作--*/
    /**
     * 交叉概率
     */
    private Double crossoverRate;
    /**
     * 变异概率
     */
    private Double mutationRate;
    /*--进化终止--*/
    /**
     * 最大迭代次数
     */
    private Integer maxGenerationCount;
    /**
     * 目标覆盖率
     */
    private Integer targetCoverage;
    /*--适应度权重--*/
    /*
     * 多样性奖励权重
     */
    private Double noveltyWeight;
    /*
     * 多样性惩罚权重
     */
    private Double diversityPenalty;
    /**
     * 基础权重-覆盖率
     */
    private Double baseWeight;
    // 空参构造器
    public ParamDTO() {}

    // 静态工厂方法创建当前配置
    public static ParamDTO current() {
        ParamDTO dto = new ParamDTO();
        dto.setCrossoverRate(GeneticAlgorithmConfig.CROSSOVER_RATE);
        dto.setMutationRate(GeneticAlgorithmConfig.MUTATION_RATE);
        dto.setInitPhase(GeneticAlgorithmConfig.INIT_PHASE);
        dto.setMaxGenerationCount(GeneticAlgorithmConfig.MAX_GENERATION_COUNT);
        dto.setTargetCoverage(GeneticAlgorithmConfig.TARGET_COVERAGE);
        dto.setNoveltyWeight(GeneticAlgorithmConfig.NOVELTY_WEIGHT);
        dto.setDiversityPenalty(GeneticAlgorithmConfig.DIVERSITY_PENALTY);
        dto.setBaseWeight(GeneticAlgorithmConfig.BASE_WEIGHT);
        dto.setConfigVersion(++VERSION);
        return dto;
    }
    public Double getCrossoverRate() {
        return crossoverRate;
    }

    public void setCrossoverRate(Double crossoverRate) {
        this.crossoverRate = crossoverRate;
    }

    public Double getMutationRate() {
        return mutationRate;
    }

    public void setMutationRate(Double mutationRate) {
        this.mutationRate = mutationRate;
    }

    public String getInitPhase() {
        return initPhase;
    }

    public void setInitPhase(String initPhase) {
        this.initPhase = initPhase;
    }

    public Integer getMaxGenerationCount() {
        return maxGenerationCount;
    }

    public void setMaxGenerationCount(Integer maxGenerationCount) {
        this.maxGenerationCount = maxGenerationCount;
    }

    public Integer getTargetCoverage() {
        return targetCoverage;
    }

    public void setTargetCoverage(Integer targetCoverage) {
        this.targetCoverage = targetCoverage;
    }

    public Double getNoveltyWeight() {
        return noveltyWeight;
    }

    public void setNoveltyWeight(Double noveltyWeight) {
        this.noveltyWeight = noveltyWeight;
    }

    public Double getDiversityPenalty() {
        return diversityPenalty;
    }

    public void setDiversityPenalty(Double diversityPenalty) {
        this.diversityPenalty = diversityPenalty;
    }

    public Double getBaseWeight() {
        return baseWeight;
    }

    public void setBaseWeight(Double baseWeight) {
        this.baseWeight = baseWeight;
    }

    public long getConfigVersion() {
        return configVersion;
    }

    public void setConfigVersion(long configVersion) {
        this.configVersion = configVersion;
    }
}