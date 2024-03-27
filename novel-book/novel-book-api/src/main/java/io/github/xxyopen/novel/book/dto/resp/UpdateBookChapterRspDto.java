package io.github.xxyopen.novel.book.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新小说章节dto
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBookChapterRspDto {

    /**
     * 章节名
     */
    private String chapterName;

    /**
     * 章节内容
     */
    private String chapterContent;

    /**
     * 是否收费
     */
    private Integer isVip;

}
