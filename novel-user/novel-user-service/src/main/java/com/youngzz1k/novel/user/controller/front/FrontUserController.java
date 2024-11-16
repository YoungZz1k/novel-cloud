package com.youngzz1k.novel.user.controller.front;

import com.youngzz1k.novel.book.dto.req.BookCommentReqDto;
import com.youngzz1k.novel.common.auth.UserHolder;
import com.youngzz1k.novel.common.constant.ApiRouterConsts;
import com.youngzz1k.novel.common.constant.SystemConfigConsts;
import com.youngzz1k.novel.common.resp.RestResp;
import com.youngzz1k.novel.user.dto.req.UserCommentsReqDto;
import com.youngzz1k.novel.user.dto.req.UserInfoUptReqDto;
import com.youngzz1k.novel.user.dto.req.UserLoginReqDto;
import com.youngzz1k.novel.user.dto.req.UserRegisterReqDto;
import com.youngzz1k.novel.user.dto.resp.UserInfoRespDto;
import com.youngzz1k.novel.user.dto.resp.UserLoginRespDto;
import com.youngzz1k.novel.user.dto.resp.UserRegisterRespDto;
import com.youngzz1k.novel.user.manager.feign.BookFeignManager;
import com.youngzz1k.novel.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 前台门户-会员模块 API 控制器
 *
 * @author YoungZz1k
 * @date 2024/11/17
 */
@Tag(name = "UserController", description = "前台门户-会员模块")
@SecurityRequirement(name = SystemConfigConsts.HTTP_AUTH_HEADER_NAME)
@RestController
@RequestMapping(ApiRouterConsts.API_FRONT_USER_URL_PREFIX)
@RequiredArgsConstructor
public class FrontUserController {

    private final UserService userService;

    private final BookFeignManager bookFeignManager;

    /**
     * 用户注册接口
     */
    @Operation(summary = "用户注册接口")
    @PostMapping("register")
    public RestResp<UserRegisterRespDto> register(@Valid @RequestBody UserRegisterReqDto dto) {
        return userService.register(dto);
    }

    /**
     * 用户登录接口
     */
    @Operation(summary = "用户登录接口")
    @PostMapping("login")
    public RestResp<UserLoginRespDto> login(@Valid @RequestBody UserLoginReqDto dto) {
        return userService.login(dto);
    }

    /**
     * 用户信息查询接口
     */
    @Operation(summary = "用户信息查询接口")
    @GetMapping
    public RestResp<UserInfoRespDto> getUserInfo() {
        return userService.getUserInfo(UserHolder.getUserId());
    }

    /**
     * 用户信息修改接口
     */
    @Operation(summary = "用户信息修改接口")
    @PutMapping
    public RestResp<Void> updateUserInfo(@Valid @RequestBody UserInfoUptReqDto dto) {
        dto.setUserId(UserHolder.getUserId());
        return userService.updateUserInfo(dto);
    }

    /**
     * 用户反馈提交接口
     */
    @Operation(summary = "用户反馈提交接口")
    @PostMapping("feedback")
    public RestResp<Void> submitFeedback(@RequestBody String content) {
        return userService.saveFeedback(UserHolder.getUserId(), content);
    }

    /**
     * 用户反馈删除接口
     */
    @Operation(summary = "用户反馈删除接口")
    @DeleteMapping("feedback/{id}")
    public RestResp<Void> deleteFeedback(@Parameter(description = "反馈ID") @PathVariable Long id) {
        return userService.deleteFeedback(UserHolder.getUserId(), id);
    }

    /**
     * 发表评论接口
     */
    @Operation(summary = "发表评论接口")
    @PostMapping("comment")
    public RestResp<Void> comment(@Valid @RequestBody BookCommentReqDto dto) {
        return bookFeignManager.publishComment(dto);
    }

    /**
     * 修改评论接口
     */
    @Operation(summary = "修改评论接口")
    @PutMapping("comment/{id}")
    public RestResp<Void> updateComment(@Parameter(description = "评论ID") @PathVariable Long id,
        String content) {
        BookCommentReqDto dto = new BookCommentReqDto();
        dto.setUserId(UserHolder.getUserId());
        dto.setCommentId(id);
        dto.setCommentContent(content);
        return bookFeignManager.updateComment(dto);
    }

    /**
     * 删除评论接口
     */
    @Operation(summary = "删除评论接口")
    @DeleteMapping("comment/{id}")
    public RestResp<Void> deleteComment(@Parameter(description = "评论ID") @PathVariable Long id) {
        BookCommentReqDto dto = new BookCommentReqDto();
        dto.setUserId(UserHolder.getUserId());
        dto.setCommentId(id);
        return bookFeignManager.deleteComment(dto);
    }

    /**
     * 查询书架状态接口 0-不在书架 1-已在书架
     */
    @Operation(summary = "查询书架状态接口")
    @GetMapping("bookshelf_status")
    public RestResp<Integer> getBookshelfStatus(@Parameter(description = "小说ID") String bookId) {
        return userService.getBookshelfStatus(UserHolder.getUserId(), bookId);
    }

    @Operation(summary = "用户评论查询接口")
    @GetMapping("/comments")
    public RestResp<List<UserCommentsReqDto>> listComments(@RequestParam Integer pageNum,@RequestParam Integer pageSize){
        return RestResp.ok(userService.listComments(UserHolder.getUserId(),pageNum,pageSize));
    }

}
