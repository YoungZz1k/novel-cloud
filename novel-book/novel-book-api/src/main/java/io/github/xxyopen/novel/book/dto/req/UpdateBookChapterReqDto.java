package io.github.xxyopen.novel.book.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateBookChapterReqDto {

    /**
     * 章节内容
     */
    private String chapterContent;

    /**
     * 章节名字
     */
    private String chapterName;

    /**
     * 是否收费
     */
    private Integer isVip;
}
