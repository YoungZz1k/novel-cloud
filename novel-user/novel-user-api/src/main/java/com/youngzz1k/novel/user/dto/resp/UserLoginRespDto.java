package com.youngzz1k.novel.user.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 用户登录 响应DTO
 * @author YoungZz1k
 * @date 2024/11/17
 */
@Data
@Builder
public class UserLoginRespDto {

    @Schema(description = "用户ID")
    private Long uid;

    @Schema(description = "用户昵称")
    private String nickName;

    @Schema(description = "用户token")
    private String token;
}
