package com.whj.generate.common.config;

import com.whj.generate.biz.Infrastructure.cache.ChromosomeCoverageTracker;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * 全局追踪器
 * @author whj
 * @date 2025-05-19 下午12:26
 */
@Configuration
public class TrackerConfig {
    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    public ChromosomeCoverageTracker coverageTracker() {
        return new ChromosomeCoverageTracker();
    }
}
