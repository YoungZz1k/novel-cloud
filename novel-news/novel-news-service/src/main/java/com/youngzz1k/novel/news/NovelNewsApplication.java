package com.youngzz1k.novel.news;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = {"com.youngzz1k.novel"})
@MapperScan("com.youngzz1k.novel.news.dao.mapper")
@EnableCaching
@EnableDiscoveryClient
public class NovelNewsApplication {

    public static void main(String[] args) {
        SpringApplication.run(NovelNewsApplication.class, args);
    }

}
