package com.youngzz1k.novel.book.controller.inner;

import com.youngzz1k.novel.book.dto.req.*;
import com.youngzz1k.novel.book.dto.resp.BookChapterRespDto;
import com.youngzz1k.novel.book.dto.resp.BookEsRespDto;
import com.youngzz1k.novel.book.dto.resp.BookInfoRespDto;
import com.youngzz1k.novel.book.dto.resp.UpdateBookChapterRspDto;
import com.youngzz1k.novel.book.service.BookService;
import com.youngzz1k.novel.common.constant.ApiRouterConsts;
import com.youngzz1k.novel.common.resp.PageRespDto;
import com.youngzz1k.novel.common.resp.RestResp;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 小说微服务内部调用接口
 *
 * @author YoungZz1k
 * @date 2024/11/29
 */
@Tag(name = "InnerBookController", description = "内部调用-小说模块")
@RestController
@RequestMapping(ApiRouterConsts.API_INNER_BOOK_URL_PREFIX)
@RequiredArgsConstructor
public class InnerBookController {

    private final BookService bookService;

    /**
     * 查询下一批保存到 ES 中的小说列表
     */
    @Operation(summary = "查询下一批保存到 ES 中的小说列表")
    @PostMapping("listNextEsBooks")
    RestResp<List<BookEsRespDto>> listNextEsBooks(@Parameter(description = "已查询的最大小说ID") @RequestBody Long maxBookId) {
        return bookService.listNextEsBooks(maxBookId);
    }

    /**
     * 批量查询小说信息
     */
    @Operation(summary = "批量查询小说信息")
    @PostMapping("listBookInfoByIds")
    RestResp<List<BookInfoRespDto>> listBookInfoByIds(@RequestBody List<Long> bookIds) {
        return bookService.listBookInfoByIds(bookIds);
    }

    /**
     * 查询最新推荐小说
     */
    @Operation(summary = "查询最新推荐小说")
    @PostMapping("listBookByHot")
    RestResp<List<BookInfoRespDto>> listBookByHot() {
        return bookService.listBookByHot();
    }

    /**
     * 发表评论接口
     */
    @Operation(summary = "发表评论接口")
    @PostMapping("publishComment")
    public RestResp<Void> publishComment(@Valid @RequestBody BookCommentReqDto dto) {
        return bookService.saveComment(dto);
    }

    /**
     * 修改评论接口
     */
    @Operation(summary = "修改评论接口")
    @PostMapping("updateComment")
    public RestResp<Void> updateComment(@Valid @RequestBody BookCommentReqDto dto) {
        return bookService.updateComment(dto);
    }

    /**
     * 删除评论接口
     */
    @Operation(summary = "删除评论接口")
    @PostMapping("deleteComment")
    public RestResp<Void> deleteComment(@RequestBody BookCommentReqDto dto) {
        return bookService.deleteComment(dto);
    }

    /**
     * 小说发布接口
     */
    @Operation(summary = "小说发布接口")
    @PostMapping("publishBook")
    public RestResp<Void> publishBook(@Valid @RequestBody BookAddReqDto dto) {
        return bookService.saveBook(dto);
    }

    /**
     * 小说发布列表查询接口
     */
    @Operation(summary = "小说发布列表查询接口")
    @PostMapping("listPublishBooks")
    public RestResp<PageRespDto<BookInfoRespDto>> listPublishBooks(@RequestBody BookPageReqDto dto) {
        return bookService.listAuthorBooks(dto);
    }

    /**
     * 小说章节发布接口
     */
    @Operation(summary = "小说章节发布接口")
    @PostMapping("publishBookChapter")
    public RestResp<Void> publishBookChapter(@Valid @RequestBody ChapterAddReqDto dto) {
        return bookService.saveBookChapter(dto);
    }

    /**
     * 小说章节发布列表查询接口
     */
    @Operation(summary = "小说章节发布列表查询接口")
    @PostMapping("listPublishBookChapters")
    public RestResp<PageRespDto<BookChapterRespDto>> listPublishBookChapters(@RequestBody ChapterPageReqDto dto) {
        return bookService.listBookChapters(dto);
    }

    @GetMapping("chapter/{chapterId}")
    @Operation(summary = "小说章节修改查询接口")
    public RestResp<UpdateBookChapterRspDto> getChapter(@PathVariable("chapterId") Long chapterId){
        return bookService.getChapter(chapterId);
    }

    @PutMapping("/chapter/{chapterId}")
    @Operation(summary = "小说章节修改接口")
    public RestResp<Void> updateBookChapter(@PathVariable("chapterId") Long chapterId,@RequestBody UpdateBookChapterReqDto updateBookChapterReqDto){
        return bookService.updateBookChapter(chapterId,updateBookChapterReqDto);
    }

    @PutMapping("/deleteChapter/{chapterId}")
    @Operation(summary = "小说章节删除接口")
    public RestResp<Boolean> deleteBookChapter(@PathVariable("chapterId") Long chapterId){
        return RestResp.ok(bookService.deleteBookChapter(chapterId));
    }

    @GetMapping("/getEsBookById")
    @Operation(summary = "查询要更新到ES的小说")
    public RestResp<BookEsRespDto> getEsBookById(@RequestParam Long bookId){
        return RestResp.ok(bookService.getEsBookById(bookId));
    }

}
