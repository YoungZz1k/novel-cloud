package com.youngzz1k.novel.home.service.impl;

import com.youngzz1k.novel.home.dto.resp.HomeBookRespDto;
import com.youngzz1k.novel.home.manager.cache.HomeBookCacheManager;
import com.youngzz1k.novel.home.service.HomeService;
import com.youngzz1k.novel.common.resp.RestResp;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 首页模块 服务实现类
 *
 * @author YoungZz1k
 * @date 2024/11/13
 */
@Service
@RequiredArgsConstructor
public class HomeServiceImpl implements HomeService {

    private final HomeBookCacheManager homeBookCacheManager;

    @Override
    public RestResp<List<HomeBookRespDto>> listHomeBooks() {
        List<HomeBookRespDto> list = homeBookCacheManager.listHomeBooks();
        if(CollectionUtils.isEmpty(list)){
            homeBookCacheManager.evictCache();
        }
        return RestResp.ok(list);
    }

}
