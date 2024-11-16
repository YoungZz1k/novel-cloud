package com.youngzz1k.novel.user.manager.feign;

import com.youngzz1k.novel.book.dto.req.BookCommentReqDto;
import com.youngzz1k.novel.book.dto.resp.BookInfoRespDto;
import com.youngzz1k.novel.book.feign.BookFeign;
import com.youngzz1k.novel.common.auth.UserHolder;
import com.youngzz1k.novel.common.resp.RestResp;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

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

    public RestResp<Void> publishComment(BookCommentReqDto dto) {
        dto.setUserId(UserHolder.getUserId());
        return bookFeign.publishComment(dto);
    }

    public RestResp<Void> updateComment(BookCommentReqDto dto) {
        dto.setUserId(UserHolder.getUserId());
        return bookFeign.updateComment(dto);
    }

    public RestResp<Void> deleteComment(BookCommentReqDto dto) {
        dto.setUserId(UserHolder.getUserId());
        return bookFeign.deleteComment(dto);
    }

    public RestResp<List<BookInfoRespDto>> listBookInfoByIds(List<Long> bookIds){
        return bookFeign.listBookInfoByIds(bookIds);
    }


}
