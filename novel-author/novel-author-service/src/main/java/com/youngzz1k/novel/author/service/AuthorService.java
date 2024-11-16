package com.youngzz1k.novel.author.service;


import com.youngzz1k.novel.author.dto.req.AuthorRegisterReqDto;
import com.youngzz1k.novel.common.resp.RestResp;

/**
 * 作家模块 业务服务类
 *
 * @author YoungZz1k
 * @date 2024/11/23
 */
public interface AuthorService {

    /**
     * 作家注册
     *
     * @param dto 注册参数
     * @return void
     */
    RestResp<Void> register(AuthorRegisterReqDto dto);

    /**
     * 查询作家状态
     *
     * @param userId 用户ID
     * @return 作家状态
     */
    RestResp<Integer> getStatus(Long userId);
}
