package com.youngzz1k.novel.news.service;

import com.youngzz1k.novel.common.resp.RestResp;
import com.youngzz1k.novel.news.dto.resp.NewsInfoRespDto;

import java.util.List;

/**
 * 新闻模块 服务类
 *
 * @author YoungZz1k
 * @date 2024/11/14
 */
public interface NewsService {

    /**
     * 最新新闻列表查询
     *
     * @return 新闻列表
     */
    RestResp<List<NewsInfoRespDto>> listLatestNews();

    /**
     * 新闻信息查询
     *
     * @param id 新闻ID
     * @return 新闻信息
     */
    RestResp<NewsInfoRespDto> getNews(Long id);
}
