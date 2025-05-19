package com.whj.generate.core.infrastructure.strategy;

import com.whj.generate.core.domain.GenePool;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * @author whj
 * @date 2025-05-19 下午11:49
 */
// 可选：配置不同策略实现的工厂
@ConditionalOnMissingBean(CombinationStrategy.class)
@Component
public class DefaultStrategyFactory implements StrategyFactory {
    @Override
    public CombinationStrategy createStrategy(GenePool genePool) {
        return new DefaultCombinationStrategy(genePool);
    }
}
