package com.whj.generate.core.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ForkJoinPool;

/**
 * @author whj
 * @date 2025-04-10 上午2:38
 */
@Configuration
public class GeneticConfig {
    @Bean
    public ForkJoinPool geneticForkJoinPool(
            @Value("${genetic.parallelism:8}") int parallelism
    ) {
        return new ForkJoinPool(parallelism);
    }
}