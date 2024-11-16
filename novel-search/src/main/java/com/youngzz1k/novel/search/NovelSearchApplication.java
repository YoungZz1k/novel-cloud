package com.youngzz1k.novel.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {"com.youngzz1k.novel"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.youngzz1k.novel.book.feign"})
public class NovelSearchApplication {

    public static void main(String[] args) {
        SpringApplication.run(NovelSearchApplication.class, args);
    }

}
