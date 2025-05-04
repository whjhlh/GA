package com.whj.generate.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * mvc 配置
 *
 * @author whj
 * @date 2025-05-04 下午11:23
 */
@Configuration
public class MvcConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 精确匹配优先
        registry.addResourceHandler("/genetic/**")
                .addResourceLocations("classpath:/static/genetic/")
                .setCachePeriod(0);

        // 兜底配置
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
    }
}
