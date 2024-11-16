package com.youngzz1k.novel.home.manager.feign;

import com.youngzz1k.novel.book.dto.resp.BookInfoRespDto;
import com.youngzz1k.novel.book.feign.BookFeign;
import com.youngzz1k.novel.common.constant.ErrorCodeEnum;
import com.youngzz1k.novel.common.resp.RestResp;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    public List<BookInfoRespDto> listBookInfoByIds(List<Long> bookIds){
        RestResp<List<BookInfoRespDto>> resp = bookFeign.listBookInfoByIds(bookIds);
        if(Objects.equals(ErrorCodeEnum.OK.getCode(),resp.getCode())){
            return resp.getData();
        }
        return new ArrayList<>(0);
    }

}
