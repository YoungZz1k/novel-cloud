package com.youngzz1k.novel.home.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.youngzz1k.novel.book.dto.resp.BookInfoRespDto;
import com.youngzz1k.novel.home.dao.entity.HomeBook;
import com.youngzz1k.novel.home.dao.mapper.HomeBookMapper;
import com.youngzz1k.novel.home.manager.feign.BookFeignManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class UpdateRecommendBookInfoJob {

    private final HomeBookMapper homeBookMapper;

    private final BookFeignManager bookFeignManager;

    @XxlJob("updateRecommendBookInfo")
    private ReturnT<String> updateRecommendBookInfo() {
        try {
            // 先查询出所有的推荐数据
            List<HomeBook> homeBooks = homeBookMapper.selectList(new LambdaQueryWrapper<HomeBook>().orderByAsc(HomeBook::getId));
            // 根据推荐数据大小按规则查询下一批需要推荐的小说信息
            List<BookInfoRespDto> bookInfoRespDtos = bookFeignManager.listBookByHot(homeBooks.size());
            // 按顺序对推荐小说进行更新
            for (int i = 0; i < homeBooks.size(); i++) {
                HomeBook homeBook = homeBooks.get(i);
                BookInfoRespDto bookInfoRespDto = bookInfoRespDtos.get(i);
                homeBook.setBookId(bookInfoRespDto.getId());
                homeBook.setUpdateTime(LocalDateTime.now());
                homeBookMapper.updateById(homeBook);
            }
            return ReturnT.SUCCESS;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
