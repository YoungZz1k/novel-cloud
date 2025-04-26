package com.youngzz1k.novel.book.feign;

import com.youngzz1k.novel.book.dto.req.*;
import com.youngzz1k.novel.book.dto.resp.BookChapterRespDto;
import com.youngzz1k.novel.book.dto.resp.BookEsRespDto;
import com.youngzz1k.novel.book.dto.resp.BookInfoRespDto;
import com.youngzz1k.novel.book.dto.resp.UpdateBookChapterRspDto;
import com.youngzz1k.novel.common.constant.ApiRouterConsts;
import com.youngzz1k.novel.common.constant.ErrorCodeEnum;
import com.youngzz1k.novel.common.resp.PageRespDto;
import com.youngzz1k.novel.common.resp.RestResp;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 小说微服务调用客户端
 *
 * @author YoungZz1k
 * @date 2024/11/29
 */
@Component
@FeignClient(value = "novel-book-service", fallback = BookFeign.BookFeignFallback.class)
public interface BookFeign {

    /**
     * 查询下一批保存到 ES 中的小说列表
     */
    @PostMapping(ApiRouterConsts.API_INNER_BOOK_URL_PREFIX + "/listNextEsBooks")
    RestResp<List<BookEsRespDto>> listNextEsBooks(Long maxBookId);

    /**
     * 批量查询小说信息
     */
    @PostMapping(ApiRouterConsts.API_INNER_BOOK_URL_PREFIX + "/listBookInfoByIds")
    RestResp<List<BookInfoRespDto>> listBookInfoByIds(List<Long> bookIds);

    /**
     * 根据热度查询小说信息
     */
    @PostMapping(ApiRouterConsts.API_INNER_BOOK_URL_PREFIX + "/listBookByHot")
    RestResp<List<BookInfoRespDto>> listBookByHot();

    /**
     * 发表评论
     */
    @PostMapping(ApiRouterConsts.API_INNER_BOOK_URL_PREFIX + "/publishComment")
    RestResp<Void> publishComment(BookCommentReqDto dto);

    /**
     * 修改评论
     */
    @PostMapping(ApiRouterConsts.API_INNER_BOOK_URL_PREFIX + "/updateComment")
    RestResp<Void> updateComment(BookCommentReqDto dto);

    /**
     * 删除评论接口
     */
    @PostMapping(ApiRouterConsts.API_INNER_BOOK_URL_PREFIX + "/deleteComment")
    RestResp<Void> deleteComment(@RequestBody BookCommentReqDto dto);

    /**
     * 小说发布接口
     */
    @PostMapping(ApiRouterConsts.API_INNER_BOOK_URL_PREFIX + "/publishBook")
    RestResp<Void> publishBook(BookAddReqDto dto);

    /**
     * 小说发布列表查询接口
     */
    @PostMapping(ApiRouterConsts.API_INNER_BOOK_URL_PREFIX + "/listPublishBooks")
    RestResp<PageRespDto<BookInfoRespDto>> listPublishBooks(BookPageReqDto dto);

    /**
     * 小说章节发布接口
     */
    @PostMapping(ApiRouterConsts.API_INNER_BOOK_URL_PREFIX + "/publishBookChapter")
    RestResp<Void> publishBookChapter(ChapterAddReqDto dto);

    /**
     * 小说章节发布列表查询接口
     */
    @PostMapping(ApiRouterConsts.API_INNER_BOOK_URL_PREFIX + "/listPublishBookChapters")
    RestResp<PageRespDto<BookChapterRespDto>> listPublishBookChapters(ChapterPageReqDto dto);

    /**
     * 小说章节修改查询接口
     */
    @GetMapping(ApiRouterConsts.API_INNER_BOOK_URL_PREFIX + "/chapter/{chapterId}")
    RestResp<UpdateBookChapterRspDto> getChapter(@PathVariable("chapterId") Long chapterId);

    /**
     * 小说章节修改接口
     */
    @PutMapping(ApiRouterConsts.API_INNER_BOOK_URL_PREFIX + "/chapter/{chapterId}")
    RestResp<Void> updateBookChapter(@PathVariable("chapterId")Long chapterId,@RequestBody UpdateBookChapterReqDto updateBookChapterReqDto);

    /**
     * 小说章节删除接口
     */
    @PutMapping(ApiRouterConsts.API_INNER_BOOK_URL_PREFIX + "/deleteChapter/{chapterId}")
    RestResp<Boolean> deleteBookChapter(@PathVariable("chapterId")Long chapterId);

    /**
     * 获取小说信息用作ES更新
     * @param bookId
     * @return
     */
    @GetMapping(ApiRouterConsts.API_INNER_BOOK_URL_PREFIX + "/getEsBookById")
    RestResp<BookEsRespDto> getEsBookById(@RequestParam Long bookId);

    @Component
    class BookFeignFallback implements BookFeign {

        @Override
        public RestResp<List<BookEsRespDto>> listNextEsBooks(Long maxBookId) {
            return RestResp.ok(new ArrayList<>(0));
        }

        @Override
        public RestResp<List<BookInfoRespDto>> listBookInfoByIds(List<Long> bookIds) {
            return RestResp.ok(new ArrayList<>(0));
        }

        @Override
        public RestResp<List<BookInfoRespDto>> listBookByHot() {
            return RestResp.ok(new ArrayList<>(0));
        }

        @Override
        public RestResp<Void> publishComment(BookCommentReqDto dto) {
            return RestResp.fail(ErrorCodeEnum.THIRD_SERVICE_ERROR);
        }

        @Override
        public RestResp<Void> updateComment(BookCommentReqDto dto) {
            return RestResp.fail(ErrorCodeEnum.THIRD_SERVICE_ERROR);
        }

        @Override
        public RestResp<Void> deleteComment(BookCommentReqDto dto) {
            return RestResp.fail(ErrorCodeEnum.THIRD_SERVICE_ERROR);
        }

        @Override
        public RestResp<Void> publishBook(BookAddReqDto dto) {
            return RestResp.fail(ErrorCodeEnum.THIRD_SERVICE_ERROR);
        }

        @Override
        public RestResp<PageRespDto<BookInfoRespDto>> listPublishBooks(BookPageReqDto dto) {
            return RestResp.ok(PageRespDto.of(dto.getPageNum(), dto.getPageSize(), 0, new ArrayList<>(0)));
        }

        @Override
        public RestResp<Void> publishBookChapter(ChapterAddReqDto dto) {
            return RestResp.fail(ErrorCodeEnum.THIRD_SERVICE_ERROR);
        }

        @Override
        public RestResp<PageRespDto<BookChapterRespDto>> listPublishBookChapters(ChapterPageReqDto dto) {
            return RestResp.ok(PageRespDto.of(dto.getPageNum(), dto.getPageSize(), 0, new ArrayList<>(0)));
        }

        @Override
        public RestResp<UpdateBookChapterRspDto> getChapter(Long chapterId) {
            return RestResp.ok(null);
        }


        @Override
        public RestResp<Void> updateBookChapter(Long chapterId, UpdateBookChapterReqDto updateBookChapterReqDto) {
            return RestResp.fail(ErrorCodeEnum.THIRD_SERVICE_ERROR);
        }

        @Override
        public RestResp<Boolean> deleteBookChapter(Long chapterId) {
            return RestResp.ok(null);
        }

        @Override
        public RestResp<BookEsRespDto> getEsBookById(Long bookId) {
            return RestResp.ok(null);
        }
    }

}
