package com.expenseTracker.common.config;

import com.expenseTracker.common.interceptor.SysLogInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final SysLogInterceptor sysLogInterceptor;

    /** 정적 리소스를 제외한 모든 요청에 시스템 로그 인터셉터 등록 */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(sysLogInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/css/**", "/js/**", "/images/**", "/favicon.ico");
    }
}
