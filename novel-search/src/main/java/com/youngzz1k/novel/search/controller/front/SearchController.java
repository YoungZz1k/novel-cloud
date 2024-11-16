package com.youngzz1k.novel.search.controller.front;

import com.youngzz1k.novel.book.dto.req.BookSearchReqDto;
import com.youngzz1k.novel.book.dto.resp.BookInfoRespDto;
import com.youngzz1k.novel.common.constant.ApiRouterConsts;
import com.youngzz1k.novel.common.resp.PageRespDto;
import com.youngzz1k.novel.common.resp.RestResp;
import com.youngzz1k.novel.search.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 前台门户-搜索模块 API 控制器
 *
 * @author YoungZz1k
 * @date 2024/11/27
 */
@Tag(name = "SearchController", description = "前台门户-搜索模块")
@RestController
@RequestMapping(ApiRouterConsts.API_FRONT_SEARCH_URL_PREFIX)
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    /**
     * 小说搜索接口
     */
    @Operation(summary = "小说搜索接口")
    @GetMapping("books")
    public RestResp<PageRespDto<BookInfoRespDto>> searchBooks(
        @ParameterObject BookSearchReqDto condition) {
        return searchService.searchBooks(condition);
    }

}
