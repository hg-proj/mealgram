package com.mealgram.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.mealgram.common.ratelimit.MealRateLimitInterceptor;

// 인터셉터 등록 설정

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final MealRateLimitInterceptor mealRateLimitInterceptor;

    public WebConfig(MealRateLimitInterceptor mealRateLimitInterceptor) {
        this.mealRateLimitInterceptor = mealRateLimitInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(mealRateLimitInterceptor).addPathPatterns("/meals");

    }

}
