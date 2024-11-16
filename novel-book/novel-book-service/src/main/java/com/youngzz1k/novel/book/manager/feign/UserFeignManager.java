package com.youngzz1k.novel.book.manager.feign;

import com.youngzz1k.novel.common.constant.ErrorCodeEnum;
import com.youngzz1k.novel.common.resp.RestResp;
import com.youngzz1k.novel.user.dto.resp.UserInfoRespDto;
import com.youngzz1k.novel.user.feign.UserFeign;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 用户微服务调用 Feign 客户端管理
 *
 * @author xiongxiaoyang
 * @date 2023/3/29
 */
@Component
@AllArgsConstructor
public class UserFeignManager {

    private final UserFeign userFeign;

    public List<UserInfoRespDto> listUserInfoByIds(List<Long> userIds) {

        RestResp<List<UserInfoRespDto>> resp = userFeign.listUserInfoByIds(userIds);
        if (Objects.equals(ErrorCodeEnum.OK.getCode(), resp.getCode())) {
            return resp.getData();
        }
        return new ArrayList<>(0);
    }


}
