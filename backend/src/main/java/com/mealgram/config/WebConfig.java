package com.mealgram.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.mealgram.common.ratelimit.MealRateLimitInterceptor;

// API 주소 접두사, 인터셉터 등록 설정

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final MealRateLimitInterceptor mealRateLimitInterceptor;

    public WebConfig(MealRateLimitInterceptor mealRateLimitInterceptor) {
        this.mealRateLimitInterceptor = mealRateLimitInterceptor;
    }

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {

        configurer.addPathPrefix("/api", HandlerTypePredicate.forAnnotation(RestController.class));

    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(mealRateLimitInterceptor).addPathPatterns("/api/meals");

    }

}
