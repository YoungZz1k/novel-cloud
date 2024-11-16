package io.github.xxyopen.novel.search.manager.feign;

import com.google.common.collect.Lists;
import io.github.xxyopen.novel.book.dto.resp.BookEsRespDto;
import io.github.xxyopen.novel.book.dto.resp.BookInfoRespDto;
import io.github.xxyopen.novel.book.feign.BookFeign;
import io.github.xxyopen.novel.common.constant.ErrorCodeEnum;
import io.github.xxyopen.novel.common.resp.RestResp;
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

    public List<BookEsRespDto> listEsBooks(Long maxBookId){
        RestResp<List<BookEsRespDto>> listRestResp = bookFeign.listNextEsBooks(maxBookId);
        if(Objects.equals(ErrorCodeEnum.OK.getCode(),listRestResp.getCode())){
            return listRestResp.getData();
        }
        return new ArrayList<>(0);
    }

    public RestResp<BookEsRespDto> getEsBookById(Long bookId){
        return bookFeign.getEsBookById(bookId);
    }

}
