package com.youngzz1k.novel.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Xss 过滤配置属性
 *
 * @author YoungZz1k
 * @date 2024/11/17
 */
@ConfigurationProperties(prefix = "novel.xss")
public record XssProperties(Boolean enabled, List<String> excludes) {

}
