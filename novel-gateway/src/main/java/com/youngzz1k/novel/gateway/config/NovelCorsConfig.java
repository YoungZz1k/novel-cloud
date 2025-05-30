package com.youngzz1k.novel.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * 全局跨域配置
 *
 * @author YoungZz1k
 * @date 2024/11/27
 */
@Configuration
public class NovelCorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // 允许的域,不要写*，否则cookie无法使用
        config.addAllowedOrigin("http://localhost:1024");
        // 允许的头信息
        config.addAllowedHeader("*");
        // 允许的请求方式
        config.addAllowedMethod("*");
        // 是否允许携带Cookie信息
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource configurationSource = new UrlBasedCorsConfigurationSource();
        // 添加映射路径，拦截一切请求
        configurationSource.registerCorsConfiguration("/**", config);
        return new CorsWebFilter(configurationSource);
    }
}
