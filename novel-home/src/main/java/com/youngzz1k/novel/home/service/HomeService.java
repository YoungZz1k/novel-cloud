package com.youngzz1k.novel.home.service;

import com.youngzz1k.novel.common.resp.RestResp;
import com.youngzz1k.novel.home.dto.resp.HomeBookRespDto;
import com.youngzz1k.novel.home.dto.resp.HomeFriendLinkRespDto;

import java.util.List;

/**
 * 首页模块 服务类
 *
 * @author YoungZz1k
 * @date 2024/11/13
 */
public interface HomeService {

    /**
     * 查询首页小说推荐列表
     *
     * @return 首页小说推荐列表的 rest 响应结果
     */
    RestResp<List<HomeBookRespDto>> listHomeBooks();

    /**
     * 首页友情链接列表查询
     *
     * @return 友情链接列表
     */
    RestResp<List<HomeFriendLinkRespDto>> listHomeFriendLinks();
}
