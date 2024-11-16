package com.youngzz1k.novel.user.feign;

import com.youngzz1k.novel.user.dto.resp.UserInfoRespDto;
import com.youngzz1k.novel.common.constant.ApiRouterConsts;
import com.youngzz1k.novel.common.resp.RestResp;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户微服务调用客户端
 *
 * @author YoungZz1k
 * @date 2024/11/29
 */
@Component
@FeignClient(value = "novel-user-service", fallback = UserFeign.UserFeignFallback.class)
public interface UserFeign {

    /**
     * 批量查询用户信息
     */
    @PostMapping(ApiRouterConsts.API_INNER_USER_URL_PREFIX + "/listUserInfoByIds")
    RestResp<List<UserInfoRespDto>> listUserInfoByIds(List<Long> userIds);

    @Component
    class UserFeignFallback implements UserFeign {

        @Override
        public RestResp<List<UserInfoRespDto>> listUserInfoByIds(List<Long> userIds) {

            return RestResp.ok(new ArrayList<>(0));

        }
    }

}
