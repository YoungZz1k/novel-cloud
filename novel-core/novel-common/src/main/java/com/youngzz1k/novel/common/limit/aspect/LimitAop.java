package com.youngzz1k.novel.common.limit.aspect;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.RateLimiter;
import com.youngzz1k.novel.common.constant.ErrorCodeEnum;
import com.youngzz1k.novel.common.limit.Limit;
import com.youngzz1k.novel.common.resp.RestResp;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;

@Slf4j
@Aspect
@Component
public class LimitAop {
    /**
     * 不同的接口，不同的流量控制
     * map的key为 Limiter.key
     */
    private final Map<String, RateLimiter> limitMap = Maps.newConcurrentMap();

    @Around("@annotation(com.youngzz1k.novel.common.limit.Limit)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        // 拿 limit 的注解
        Limit limit = method.getAnnotation(Limit.class);
        if (limit != null) {
            // key 作用：不同的接口，不同的流量控制
            String key = limit.key();
            // 使用 computeIfAbsent 保证原子性
            RateLimiter rateLimiter = limitMap.computeIfAbsent(key, k -> {
                log.info("新建了令牌桶={}，容量={}", k, limit.permitsPerSecond());
                return RateLimiter.create(limit.permitsPerSecond());
            });
            // 拿令牌
            boolean acquire = rateLimiter.tryAcquire(limit.timeout(), limit.timeunit());
            // 拿不到令牌，直接返回异常提示
            if (!acquire) {
                log.debug("令牌桶={}，获取令牌失败", key);
                return RestResp.fail(ErrorCodeEnum.USER_REQ_MANY);
            }
        }
        return joinPoint.proceed();
    }

}