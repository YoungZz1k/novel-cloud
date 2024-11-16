package io.github.xxyopen.novel.book.dto.req;

import io.github.xxyopen.novel.common.req.PageReqDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 章节发布页 请求DTO
 *
 * @author YoungZz1k
 * @date 2024/11/23
 */
@Data
public class BookPageReqDto extends PageReqDto {

    /**
     * 作家ID
     */
    @Schema(description = "作家ID", required = true)
    private Long authorId;


}
