package com.youngzz1k.novel.resource.manager.redis;

import com.youngzz1k.novel.common.constant.CacheConsts;
import com.youngzz1k.novel.resource.util.ImgVerifyCodeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;

/**
 * 验证码 管理类
 *
 * @author YoungZz1k
 * @date 2024/11/12
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class VerifyCodeManager {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 生成图形验证码，并放入 Redis 中
     */
    public String genImgVerifyCode(String sessionId) throws IOException {
        String verifyCode = ImgVerifyCodeUtils.getRandomVerifyCode(4);
        String img = ImgVerifyCodeUtils.genVerifyCodeImg(verifyCode);
        stringRedisTemplate.opsForValue().set(CacheConsts.IMG_VERIFY_CODE_CACHE_KEY + sessionId,
            verifyCode, Duration.ofMinutes(2));
        return img;
    }

}
