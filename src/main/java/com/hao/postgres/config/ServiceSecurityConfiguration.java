package com.hao.postgres.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Import this configuration for any services which will be accessed through API-Gateway or from other services
 */
@Configuration
public class ServiceSecurityConfiguration implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new ServiceSecurityInterceptor()).order(0)
            .addPathPatterns("/**").excludePathPatterns("/error");
    }

}