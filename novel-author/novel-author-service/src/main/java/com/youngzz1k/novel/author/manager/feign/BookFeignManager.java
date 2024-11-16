package com.youngzz1k.novel.author.manager.feign;

import com.youngzz1k.novel.author.dto.AuthorInfoDto;
import com.youngzz1k.novel.author.manager.cache.AuthorInfoCacheManager;
import com.youngzz1k.novel.book.dto.req.*;
import com.youngzz1k.novel.book.dto.resp.BookChapterRespDto;
import com.youngzz1k.novel.book.dto.resp.BookInfoRespDto;
import com.youngzz1k.novel.book.dto.resp.UpdateBookChapterRspDto;
import com.youngzz1k.novel.book.feign.BookFeign;
import com.youngzz1k.novel.common.auth.UserHolder;
import com.youngzz1k.novel.common.resp.PageRespDto;
import com.youngzz1k.novel.common.resp.RestResp;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 小说微服务调用 Feign 客户端管理
 *
 * @author YoungZz1k
 * @date 2024/11/29
 */
@Component
@AllArgsConstructor
public class BookFeignManager {

    private final BookFeign bookFeign;

    private final AuthorInfoCacheManager authorInfoCacheManager;

    public RestResp<Void> publishBook(BookAddReqDto dto) {
        AuthorInfoDto author = authorInfoCacheManager.getAuthor(UserHolder.getUserId());
        dto.setAuthorId(author.getId());
        dto.setPenName(author.getPenName());
        return bookFeign.publishBook(dto);
    }

    public RestResp<PageRespDto<BookInfoRespDto>> listPublishBooks(BookPageReqDto dto) {
        authorInfoCacheManager.getAuthor(UserHolder.getUserId());
        return bookFeign.listPublishBooks(dto);
    }

    public RestResp<Void> publishBookChapter(ChapterAddReqDto dto) {
        return bookFeign.publishBookChapter(dto);
    }

    public RestResp<PageRespDto<BookChapterRespDto>> listPublishBookChapters(ChapterPageReqDto dto) {
        return bookFeign.listPublishBookChapters(dto);
    }

    public RestResp<UpdateBookChapterRspDto> getChapter(Long chapterId){
        return bookFeign.getChapter(chapterId);
    }


    public RestResp<Void> updateBookChapter(Long chapterId, UpdateBookChapterReqDto updateBookChapterReqDto) {
        return bookFeign.updateBookChapter(chapterId,updateBookChapterReqDto);
    }

    public RestResp<Boolean> deleteBookChapter(Long chapterId) {
        return bookFeign.deleteBookChapter(chapterId);
    }
}
