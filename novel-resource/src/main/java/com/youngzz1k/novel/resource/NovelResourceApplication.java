package com.youngzz1k.novel.resource;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = {"com.youngzz1k.novel"})
@EnableDiscoveryClient
public class NovelResourceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NovelResourceApplication.class, args);
    }

}
