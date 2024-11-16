package com.youngzz1k.novel.news.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.youngzz1k.novel.news.service.NewsService;
import com.youngzz1k.novel.common.constant.DatabaseConsts;
import com.youngzz1k.novel.common.resp.RestResp;
import com.youngzz1k.novel.news.dao.entity.NewsContent;
import com.youngzz1k.novel.news.dao.entity.NewsInfo;
import com.youngzz1k.novel.news.dao.mapper.NewsContentMapper;
import com.youngzz1k.novel.news.dao.mapper.NewsInfoMapper;
import com.youngzz1k.novel.news.dto.resp.NewsInfoRespDto;
import com.youngzz1k.novel.news.manager.cache.NewsCacheManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 新闻模块 服务实现类
 *
 * @author YoungZz1k
 * @date 2024/11/14
 */
@Service
@RequiredArgsConstructor
public class NewsServiceImpl implements NewsService {

    private final NewsCacheManager newsCacheManager;

    private final NewsInfoMapper newsInfoMapper;

    private final NewsContentMapper newsContentMapper;

    @Override
    public RestResp<List<NewsInfoRespDto>> listLatestNews() {
        return RestResp.ok(newsCacheManager.listLatestNews());
    }

    @Override
    public RestResp<NewsInfoRespDto> getNews(Long id) {
        NewsInfo newsInfo = newsInfoMapper.selectById(id);
        QueryWrapper<NewsContent> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.NewsContentTable.COLUMN_NEWS_ID, id)
            .last(DatabaseConsts.SqlEnum.LIMIT_1.getSql());
        NewsContent newsContent = newsContentMapper.selectOne(queryWrapper);
        return RestResp.ok(NewsInfoRespDto.builder()
            .title(newsInfo.getTitle())
            .sourceName(newsInfo.getSourceName())
            .updateTime(newsInfo.getUpdateTime())
            .content(newsContent.getContent())
            .build());
    }
}
