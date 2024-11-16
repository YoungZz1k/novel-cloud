package com.youngzz1k.novel.search.service;


import com.youngzz1k.novel.book.dto.req.BookSearchReqDto;
import com.youngzz1k.novel.book.dto.resp.BookInfoRespDto;
import com.youngzz1k.novel.common.resp.PageRespDto;
import com.youngzz1k.novel.common.resp.RestResp;

/**
 * 搜索 服务类
 *
 * @author YoungZz1k
 * @date 2024/11/23
 */
public interface SearchService {

    /**
     * 小说搜索
     *
     * @param condition 搜索条件
     * @return 搜索结果
     */
    RestResp<PageRespDto<BookInfoRespDto>> searchBooks(BookSearchReqDto condition);

}
