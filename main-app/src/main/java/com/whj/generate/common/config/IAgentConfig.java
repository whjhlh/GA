package com.whj.generate.common.config;

import org.jacoco.agent.rt.IAgent;
import org.jacoco.agent.rt.RT;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * @author whj
 * @date 2025-05-19 下午12:59
 */
@Configuration
public class IAgentConfig {
    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    public IAgent agent() {
        return RT.getAgent();
    }
}
