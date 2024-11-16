package com.youngzz1k.novel.user.dto.req;

import lombok.Data;

import java.sql.Date;
import java.time.LocalDateTime;

@Data
public class UserCommentsReqDto {

    private Long commentId;

    private String commentBook;

    private String commentBookPic;

    private String commentContent;

    private LocalDateTime commentTime;
}
