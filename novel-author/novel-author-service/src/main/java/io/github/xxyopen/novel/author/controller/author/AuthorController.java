package io.github.xxyopen.novel.author.controller.author;

import io.github.xxyopen.novel.author.dto.req.AuthorRegisterReqDto;
import io.github.xxyopen.novel.author.manager.feign.BookFeignManager;
import io.github.xxyopen.novel.author.service.AuthorService;
import io.github.xxyopen.novel.book.dto.req.*;
import io.github.xxyopen.novel.book.dto.resp.BookChapterRespDto;
import io.github.xxyopen.novel.book.dto.resp.BookContentAboutRespDto;
import io.github.xxyopen.novel.book.dto.resp.BookInfoRespDto;
import io.github.xxyopen.novel.book.dto.resp.UpdateBookChapterRspDto;
import io.github.xxyopen.novel.common.auth.UserHolder;
import io.github.xxyopen.novel.common.constant.ApiRouterConsts;
import io.github.xxyopen.novel.common.constant.SystemConfigConsts;
import io.github.xxyopen.novel.common.req.PageReqDto;
import io.github.xxyopen.novel.common.resp.PageRespDto;
import io.github.xxyopen.novel.common.resp.RestResp;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

/**
 * 作家后台-作家模块 API 控制器
 *
 * @author xiongxiaoyang
 * @date 2022/5/23
 */
@Tag(name = "AuthorController", description = "作家后台-作者模块")
@SecurityRequirement(name = SystemConfigConsts.HTTP_AUTH_HEADER_NAME)
@RestController
@RequestMapping(ApiRouterConsts.API_AUTHOR_URL_PREFIX)
@RequiredArgsConstructor
public class AuthorController {

    private final AuthorService authorService;

    private final BookFeignManager bookFeignManager;

    /**
     * 作家注册接口
     */
    @Operation(summary = "作家注册接口")
    @PostMapping("register")
    public RestResp<Void> register(@Valid @RequestBody AuthorRegisterReqDto dto) {
        dto.setUserId(UserHolder.getUserId());
        return authorService.register(dto);
    }

    /**
     * 查询作家状态接口
     */
    @Operation(summary = "作家状态查询接口")
    @GetMapping("status")
    public RestResp<Integer> getStatus() {
        return authorService.getStatus(UserHolder.getUserId());
    }

    /**
     * 小说发布接口
     */
    @Operation(summary = "小说发布接口")
    @PostMapping("book")
    public RestResp<Void> publishBook(@Valid @RequestBody BookAddReqDto dto) {
        return bookFeignManager.publishBook(dto);
    }

    /**
     * 小说发布列表查询接口
     */
    @Operation(summary = "小说发布列表查询接口")
    @GetMapping("books")
    public RestResp<PageRespDto<BookInfoRespDto>> listBooks(@ParameterObject BookPageReqDto dto) {
        dto.setAuthorId(UserHolder.getAuthorId());
        return bookFeignManager.listPublishBooks(dto);
    }

    /**
     * 小说章节发布接口
     */
    @Operation(summary = "小说章节发布接口")
    @PostMapping("book/chapter/{bookId}")
    public RestResp<Void> publishBookChapter(@Parameter(description = "小说ID") @PathVariable("bookId") Long bookId,@Valid @RequestBody ChapterAddReqDto dto) {
        dto.setAuthorId(UserHolder.getAuthorId());
        dto.setBookId(bookId);
        return bookFeignManager.publishBookChapter(dto);
    }

    @Operation(summary = "小说章节修改查询接口")
    @GetMapping("book/chapter/{chapterId}")
    public RestResp<UpdateBookChapterRspDto> getChapter(@PathVariable("chapterId") Long chapterId){
        return bookFeignManager.getChapter(chapterId);
    }

    /**
     * 小说章节发布列表查询接口
     */
    @Operation(summary = "小说章节发布列表查询接口")
    @GetMapping("book/chapters/{bookId}")
    public RestResp<PageRespDto<BookChapterRespDto>> listBookChapters(
        @Parameter(description = "小说ID") @PathVariable("bookId") Long bookId,
        @ParameterObject PageReqDto dto) {
        ChapterPageReqDto chapterPageReqReqDto = new ChapterPageReqDto();
        chapterPageReqReqDto.setBookId(bookId);
        chapterPageReqReqDto.setPageNum(dto.getPageNum());
        chapterPageReqReqDto.setPageSize(dto.getPageSize());
        return bookFeignManager.listPublishBookChapters(chapterPageReqReqDto);
    }


    @Operation(summary = "小说章节修改接口")
    @PutMapping("book/chapter/{chapterId}")
    public RestResp<Void> UpdateBookChapter(@PathVariable Long chapterId, @RequestBody UpdateBookChapterReqDto updateBookChapterReqDto){
        bookFeignManager.updateBookChapter(chapterId,updateBookChapterReqDto);
        return RestResp.ok();
    }

    @PutMapping("book/deleteChapter/{chapterId}")
    @Operation(summary = "小说章节删除接口")
    public RestResp<Boolean> deleteBookChapter(@PathVariable("chapterId") Long chapterId){
        return bookFeignManager.deleteBookChapter(chapterId);
    }


}
