package com.youngzz1k.novel.config;

import com.youngzz1k.novel.common.constant.ApiRouterConsts;
import com.youngzz1k.novel.config.interceptor.FlowLimitInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration("sentinelWebConfig")
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private final FlowLimitInterceptor flowLimitInterceptor;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 流量限制拦截器
        registry.addInterceptor(flowLimitInterceptor)
                .addPathPatterns("/**")
                .order(0);
    }
}
