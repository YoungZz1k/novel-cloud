package com.youngzz1k.novel.user.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.youngzz1k.novel.user.manager.cache.UserInfoCacheManager;
import com.youngzz1k.novel.common.auth.JwtUtils;
import com.youngzz1k.novel.common.auth.UserHolder;
import com.youngzz1k.novel.common.constant.ErrorCodeEnum;
import com.youngzz1k.novel.common.constant.SystemConfigConsts;
import com.youngzz1k.novel.config.exception.BusinessException;
import com.youngzz1k.novel.user.dto.UserInfoDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.Objects;

/**
 * 认证授权 拦截器：为了注入其它的 Spring beans，需要通过 @Component 注解将该拦截器注册到 Spring 上下文
 *
 * @author YoungZz1k
 * @date 2024/11/18
 */
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper;

    private final UserInfoCacheManager userInfoCacheManager;

    /**
     * handle 执行前调用
     */
    @SuppressWarnings("NullableProblems")
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
        Object handler) throws Exception {
        // 获取登录 JWT
        String token = request.getHeader(SystemConfigConsts.HTTP_AUTH_HEADER_NAME);

        // 开始认证
        if (!StringUtils.hasText(token)) {
            // token 为空
            throw new BusinessException(ErrorCodeEnum.USER_LOGIN_EXPIRED);
        }
        Long userId = JwtUtils.parseToken(token, SystemConfigConsts.NOVEL_FRONT_KEY);
        if (Objects.isNull(userId)) {
            // token 解析失败
            throw new BusinessException(ErrorCodeEnum.USER_LOGIN_EXPIRED);
        }
        UserInfoDto userInfo = userInfoCacheManager.getUser(userId);
        if (Objects.isNull(userInfo)) {
            // 用户不存在
            throw new BusinessException(ErrorCodeEnum.USER_ACCOUNT_NOT_EXIST);
        }
        // 设置 userId 到当前线程
        UserHolder.setUserId(userId);
        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

    /**
     * handler 执行后调用，出现异常不调用
     */
    @SuppressWarnings("NullableProblems")
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
        ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    /**
     * DispatcherServlet 完全处理完请求后调用，出现异常照常调用
     */
    @SuppressWarnings("NullableProblems")
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
        throws Exception {
        // 清理当前线程保存的用户数据
        UserHolder.clear();
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }

}
