package com.youngzz1k.novel.user.controller.inner;

import com.youngzz1k.novel.common.constant.ApiRouterConsts;
import com.youngzz1k.novel.common.resp.RestResp;
import com.youngzz1k.novel.user.dto.resp.UserInfoRespDto;
import com.youngzz1k.novel.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户微服务内部调用接口
 *
 * @author YoungZz1k
 * @date 2024/11/29
 */
@Tag(name = "InnerBookController", description = "内部调用-用户模块")
@RestController
@RequestMapping(ApiRouterConsts.API_INNER_USER_URL_PREFIX)
@RequiredArgsConstructor
public class InnerUserController {

    private final UserService userService;

    /**
     * 批量查询用户信息
     */
    @Operation(summary = "批量查询用户信息")
    @PostMapping("listUserInfoByIds")
    RestResp<List<UserInfoRespDto>> listUserInfoByIds(@RequestBody List<Long> userIds) {
        return userService.listUserInfoByIds(userIds);
    }



}
