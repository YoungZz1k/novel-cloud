package com.youngzz1k.novel.book.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 小说分类 响应DTO
 *
 * @author YoungZz1k
 * @date 2024/11/16
 */
@Data
@Builder
public class BookCategoryRespDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 6002657318451606664L;

    /**
     * 类别ID
     */
    @Schema(description = "类别ID")
    private Long id;

    /**
     * 类别名
     */
    @Schema(description = "类别名")
    private String name;

}
