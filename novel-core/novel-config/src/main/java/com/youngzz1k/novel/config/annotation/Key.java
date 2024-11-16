package com.youngzz1k.novel.config.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 分布式锁-Key 注解
 *
 * @author YoungZz1k
 * @date 2024/11/20
 */
@Documented
@Retention(RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
public @interface Key {

    String expr() default "";

}
