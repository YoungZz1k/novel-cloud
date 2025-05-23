package com.youngzz1k.novel.book.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youngzz1k.novel.book.dao.entity.BookChapter;
import com.youngzz1k.novel.book.dao.entity.BookComment;
import com.youngzz1k.novel.book.dao.entity.BookContent;
import com.youngzz1k.novel.book.dao.entity.BookInfo;
import com.youngzz1k.novel.book.dao.mapper.BookChapterMapper;
import com.youngzz1k.novel.book.dao.mapper.BookCommentMapper;
import com.youngzz1k.novel.book.dao.mapper.BookContentMapper;
import com.youngzz1k.novel.book.dao.mapper.BookInfoMapper;
import com.youngzz1k.novel.book.dto.req.*;
import com.youngzz1k.novel.book.dto.resp.*;
import com.youngzz1k.novel.book.manager.cache.*;
import com.youngzz1k.novel.book.manager.feign.UserFeignManager;
import com.youngzz1k.novel.book.manager.mq.AmqpMsgManager;
import com.youngzz1k.novel.book.service.BookService;
import com.youngzz1k.novel.common.auth.UserHolder;
import com.youngzz1k.novel.common.constant.DatabaseConsts;
import com.youngzz1k.novel.common.constant.ErrorCodeEnum;
import com.youngzz1k.novel.common.resp.PageRespDto;
import com.youngzz1k.novel.common.resp.RestResp;
import com.youngzz1k.novel.config.annotation.Key;
import com.youngzz1k.novel.config.annotation.Lock;
import com.youngzz1k.novel.user.dto.resp.UserInfoRespDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 小说模块 服务实现类
 *
 * @author YoungZz1k
 * @date 2024/11/14
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BookServiceImpl implements BookService {

    private final BookCategoryCacheManager bookCategoryCacheManager;

    private final BookRankCacheManager bookRankCacheManager;

    private final BookInfoCacheManager bookInfoCacheManager;

    private final BookChapterCacheManager bookChapterCacheManager;

    private final BookContentCacheManager bookContentCacheManager;

    private final BookInfoMapper bookInfoMapper;

    private final BookChapterMapper bookChapterMapper;

    private final BookContentMapper bookContentMapper;

    private final BookCommentMapper bookCommentMapper;

    private final AmqpMsgManager amqpMsgManager;

    private final UserFeignManager userFeignManager;

    private static final Integer REC_BOOK_COUNT = 4;

    @Override
    public RestResp<List<BookRankRespDto>> listVisitRankBooks() {
        return RestResp.ok(bookRankCacheManager.listVisitRankBooks());
    }

    @Override
    public RestResp<List<BookRankRespDto>> listNewestRankBooks() {
        return RestResp.ok(bookRankCacheManager.listNewestRankBooks());
    }

    @Override
    public RestResp<List<BookRankRespDto>> listUpdateRankBooks() {
        return RestResp.ok(bookRankCacheManager.listUpdateRankBooks());
    }

    @Override
    public RestResp<BookInfoRespDto> getBookById(Long bookId) {
        return RestResp.ok(bookInfoCacheManager.getBookInfo(bookId));
    }

    @Override
    public RestResp<BookChapterAboutRespDto> getLastChapterAbout(Long bookId) {
        // 查询小说信息
        BookInfoRespDto bookInfo = bookInfoCacheManager.getBookInfo(bookId);

        // 查询最新章节信息
        BookChapterRespDto bookChapter = bookChapterCacheManager.getChapter(
                bookInfo.getLastChapterId());

        // 查询章节内容
        String content = bookContentCacheManager.getBookContent(bookInfo.getLastChapterId());

        // 查询章节总数
        QueryWrapper<BookChapter> chapterQueryWrapper = new QueryWrapper<>();
        chapterQueryWrapper.eq(DatabaseConsts.BookChapterTable.COLUMN_BOOK_ID, bookId);
        Long chapterTotal = bookChapterMapper.selectCount(chapterQueryWrapper);

        // 组装数据并返回
        return RestResp.ok(BookChapterAboutRespDto.builder()
                .chapterInfo(bookChapter)
                .chapterTotal(chapterTotal)
                .contentSummary(content.substring(0, 30))
                .build());
    }

    @Override
    public RestResp<List<BookInfoRespDto>> listRecBooks(Long bookId)
            throws NoSuchAlgorithmException {
        Long categoryId = bookInfoCacheManager.getBookInfo(bookId).getCategoryId();
        List<Long> lastUpdateIdList = bookInfoCacheManager.getLastUpdateIdList(categoryId);
        List<BookInfoRespDto> respDtoList = new ArrayList<>();
        List<Integer> recIdIndexList = new ArrayList<>();
        int count = 0;
        Random rand = SecureRandom.getInstanceStrong();
        while (count < REC_BOOK_COUNT) {
            int recIdIndex = rand.nextInt(lastUpdateIdList.size());
            if (!recIdIndexList.contains(recIdIndex)) {
                recIdIndexList.add(recIdIndex);
                bookId = lastUpdateIdList.get(recIdIndex);
                BookInfoRespDto bookInfo = bookInfoCacheManager.getBookInfo(bookId);
                respDtoList.add(bookInfo);
                count++;
            }
        }
        return RestResp.ok(respDtoList);
    }

    @Override
    public RestResp<Void> addVisitCount(Long bookId) {
        bookInfoMapper.addVisitCount(bookId);
        return RestResp.ok();
    }

    @Override
    public RestResp<Long> getPreChapterId(Long chapterId) {
        // 查询小说ID 和 章节号
        BookChapterRespDto chapter = bookChapterCacheManager.getChapter(chapterId);
        Long bookId = chapter.getBookId();
        Integer chapterNum = chapter.getChapterNum();

        // 查询上一章信息并返回章节ID
        QueryWrapper<BookChapter> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.BookChapterTable.COLUMN_BOOK_ID, bookId)
                .lt(DatabaseConsts.BookChapterTable.COLUMN_CHAPTER_NUM, chapterNum)
                .orderByDesc(DatabaseConsts.BookChapterTable.COLUMN_CHAPTER_NUM)
                .last(DatabaseConsts.SqlEnum.LIMIT_1.getSql());
        return RestResp.ok(
                Optional.ofNullable(bookChapterMapper.selectOne(queryWrapper))
                        .map(BookChapter::getId)
                        .orElse(null)
        );
    }

    @Override
    public RestResp<Long> getNextChapterId(Long chapterId) {
        // 查询小说ID 和 章节号
        BookChapterRespDto chapter = bookChapterCacheManager.getChapter(chapterId);
        Long bookId = chapter.getBookId();
        Integer chapterNum = chapter.getChapterNum();

        // 查询下一章信息并返回章节ID
        QueryWrapper<BookChapter> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.BookChapterTable.COLUMN_BOOK_ID, bookId)
                .gt(DatabaseConsts.BookChapterTable.COLUMN_CHAPTER_NUM, chapterNum)
                .orderByAsc(DatabaseConsts.BookChapterTable.COLUMN_CHAPTER_NUM)
                .last(DatabaseConsts.SqlEnum.LIMIT_1.getSql());
        return RestResp.ok(
                Optional.ofNullable(bookChapterMapper.selectOne(queryWrapper))
                        .map(BookChapter::getId)
                        .orElse(null)
        );
    }

    @Override
    public RestResp<List<BookChapterRespDto>> listChapters(Long bookId) {
        QueryWrapper<BookChapter> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.BookChapterTable.COLUMN_BOOK_ID, bookId)
                .orderByAsc(DatabaseConsts.BookChapterTable.COLUMN_CHAPTER_NUM);
        return RestResp.ok(bookChapterMapper.selectList(queryWrapper).stream()
                .map(v -> BookChapterRespDto.builder()
                        .id(v.getId())
                        .chapterName(v.getChapterName())
                        .isVip(v.getIsVip())
                        .build()).toList());
    }

    @Override
    public RestResp<List<BookCategoryRespDto>> listCategory(Integer workDirection) {
        return RestResp.ok(bookCategoryCacheManager.listCategory(workDirection));
    }

    @Lock(prefix = "userComment")
    @Override
    public RestResp<Void> saveComment(
            @Key(expr = "#{userId + '::' + bookId}") BookCommentReqDto dto) {
        // 校验用户是否已发表评论
        QueryWrapper<BookComment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.BookCommentTable.COLUMN_USER_ID, dto.getUserId())
                .eq(DatabaseConsts.BookCommentTable.COLUMN_BOOK_ID, dto.getBookId());
        if (bookCommentMapper.selectCount(queryWrapper) > 0) {
            // 用户已发表评论
            return RestResp.fail(ErrorCodeEnum.USER_COMMENTED);
        }
        BookComment bookComment = new BookComment();
        bookComment.setBookId(dto.getBookId());
        bookComment.setUserId(dto.getUserId());
        bookComment.setCommentContent(dto.getCommentContent());
        bookComment.setCreateTime(LocalDateTime.now());
        bookComment.setUpdateTime(LocalDateTime.now());
        bookCommentMapper.insert(bookComment);
        return RestResp.ok();
    }

    @Override
    public RestResp<BookCommentRespDto> listNewestComments(Long bookId) {
        // 查询评论总数
        QueryWrapper<BookComment> commentCountQueryWrapper = new QueryWrapper<>();
        commentCountQueryWrapper.eq(DatabaseConsts.BookCommentTable.COLUMN_BOOK_ID, bookId);
        Long commentTotal = bookCommentMapper.selectCount(commentCountQueryWrapper);
        BookCommentRespDto bookCommentRespDto = BookCommentRespDto.builder()
                .commentTotal(commentTotal).build();
        if (commentTotal > 0) {

            // 查询最新的评论列表
            QueryWrapper<BookComment> commentQueryWrapper = new QueryWrapper<>();
            commentQueryWrapper.eq(DatabaseConsts.BookCommentTable.COLUMN_BOOK_ID, bookId)
                    .orderByDesc(DatabaseConsts.CommonColumnEnum.CREATE_TIME.getName())
                    .last(DatabaseConsts.SqlEnum.LIMIT_5.getSql());
            List<BookComment> bookComments = bookCommentMapper.selectList(commentQueryWrapper);

            // 查询评论用户信息，并设置需要返回的评论用户名
            List<Long> userIds = bookComments.stream().map(BookComment::getUserId).toList();
            List<UserInfoRespDto> userInfos = userFeignManager.listUserInfoByIds(userIds);
            Map<Long, UserInfoRespDto> userInfoMap = userInfos.stream()
                    .collect(Collectors.toMap(UserInfoRespDto::getId, Function.identity()));
            List<BookCommentRespDto.CommentInfo> commentInfos = bookComments.stream()
                    .map(v -> BookCommentRespDto.CommentInfo.builder()
                            .id(v.getId())
                            .commentUserId(v.getUserId())
                            .commentUser(userInfoMap.get(v.getUserId()).getUsername())
                            .commentUserPhoto(userInfoMap.get(v.getUserId()).getUserPhoto())
                            .commentContent(v.getCommentContent())
                            .commentTime(v.getCreateTime()).build()).toList();
            bookCommentRespDto.setComments(commentInfos);
        } else {
            bookCommentRespDto.setComments(Collections.emptyList());
        }
        return RestResp.ok(bookCommentRespDto);
    }

    @Override
    public RestResp<Void> deleteComment(BookCommentReqDto dto) {
        QueryWrapper<BookComment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.CommonColumnEnum.ID.getName(), dto.getCommentId())
                .eq(DatabaseConsts.BookCommentTable.COLUMN_USER_ID, dto.getUserId());
        bookCommentMapper.delete(queryWrapper);
        return RestResp.ok();
    }

    @Override
    public RestResp<Void> updateComment(BookCommentReqDto dto) {
        QueryWrapper<BookComment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.CommonColumnEnum.ID.getName(), dto.getCommentId())
                .eq(DatabaseConsts.BookCommentTable.COLUMN_USER_ID, dto.getUserId());
        BookComment bookComment = new BookComment();
        bookComment.setCommentContent(dto.getCommentContent());
        bookCommentMapper.update(bookComment, queryWrapper);
        return RestResp.ok();
    }

    @Override
    public RestResp<Void> saveBook(BookAddReqDto dto) {
        // 校验小说名是否已存在
        QueryWrapper<BookInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.BookTable.COLUMN_BOOK_NAME, dto.getBookName());
        if (bookInfoMapper.selectCount(queryWrapper) > 0) {
            return RestResp.fail(ErrorCodeEnum.AUTHOR_BOOK_NAME_EXIST);
        }
        BookInfo bookInfo = new BookInfo();
        // 设置作家信息
        bookInfo.setAuthorId(dto.getAuthorId());
        bookInfo.setAuthorName(dto.getPenName());
        // 设置其他信息
        bookInfo.setWorkDirection(dto.getWorkDirection());
        bookInfo.setCategoryId(dto.getCategoryId());
        bookInfo.setCategoryName(dto.getCategoryName());
        bookInfo.setBookName(dto.getBookName());
        bookInfo.setPicUrl(dto.getPicUrl());
        bookInfo.setBookDesc(dto.getBookDesc());
        bookInfo.setIsVip(dto.getIsVip());
        bookInfo.setScore(0);
        bookInfo.setCreateTime(LocalDateTime.now());
        bookInfo.setUpdateTime(LocalDateTime.now());
        // 保存小说信息
        bookInfoMapper.insert(bookInfo);
        return RestResp.ok();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public RestResp<Void> saveBookChapter(ChapterAddReqDto dto) {
        // 校验该作品是否属于当前作家
        BookInfo bookInfo = bookInfoMapper.selectById(dto.getBookId());
        if (!Objects.equals(bookInfo.getAuthorId(), dto.getAuthorId())) {
            return RestResp.fail(ErrorCodeEnum.USER_UN_AUTH);
        }
        // 1) 保存章节相关信息到小说章节表
        //  a) 查询最新章节号
        int chapterNum = 0;
        QueryWrapper<BookChapter> chapterQueryWrapper = new QueryWrapper<>();
        chapterQueryWrapper.eq(DatabaseConsts.BookChapterTable.COLUMN_BOOK_ID, dto.getBookId())
                .orderByDesc(DatabaseConsts.BookChapterTable.COLUMN_CHAPTER_NUM)
                .last(DatabaseConsts.SqlEnum.LIMIT_1.getSql());
        BookChapter bookChapter = bookChapterMapper.selectOne(chapterQueryWrapper);
        if (Objects.nonNull(bookChapter)) {
            chapterNum = bookChapter.getChapterNum() + 1;
        }
        //  b) 设置章节相关信息并保存
        BookChapter newBookChapter = new BookChapter();
        newBookChapter.setBookId(dto.getBookId());
        newBookChapter.setChapterName(dto.getChapterName());
        newBookChapter.setChapterNum(chapterNum);
        newBookChapter.setWordCount(dto.getChapterContent().length());
        newBookChapter.setIsVip(dto.getIsVip());
        newBookChapter.setCreateTime(LocalDateTime.now());
        newBookChapter.setUpdateTime(LocalDateTime.now());
        bookChapterMapper.insert(newBookChapter);

        // 2) 保存章节内容到小说内容表
        BookContent bookContent = new BookContent();
        bookContent.setContent(dto.getChapterContent());
        bookContent.setChapterId(newBookChapter.getId());
        bookContent.setCreateTime(LocalDateTime.now());
        bookContent.setUpdateTime(LocalDateTime.now());
        bookContentMapper.insert(bookContent);

        // 3) 更新小说表最新章节信息和小说总字数信息
        //  a) 更新小说表关于最新章节的信息
        BookInfo newBookInfo = new BookInfo();
        newBookInfo.setId(dto.getBookId());
        newBookInfo.setLastChapterId(newBookChapter.getId());
        newBookInfo.setLastChapterName(newBookChapter.getChapterName());
        newBookInfo.setLastChapterUpdateTime(LocalDateTime.now());
        newBookInfo.setWordCount(bookInfo.getWordCount() + newBookChapter.getWordCount());
        newBookChapter.setUpdateTime(LocalDateTime.now());
        bookInfoMapper.updateById(newBookInfo);
        //  b) 清除小说信息缓存
        bookInfoCacheManager.evictBookInfoCache(dto.getBookId());
        //  c) 发送小说信息更新的 MQ 消息
        amqpMsgManager.sendBookChangeMsg(dto.getBookId());
        return RestResp.ok();
    }

    @Override
    public RestResp<PageRespDto<BookInfoRespDto>> listAuthorBooks(BookPageReqDto dto) {
        IPage<BookInfo> page = new Page<>();
        page.setCurrent(dto.getPageNum());
        page.setSize(dto.getPageSize());
        QueryWrapper<BookInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.BookTable.AUTHOR_ID, dto.getAuthorId())
                .orderByDesc(DatabaseConsts.CommonColumnEnum.CREATE_TIME.getName());
        IPage<BookInfo> bookInfoPage = bookInfoMapper.selectPage(page, queryWrapper);
        return RestResp.ok(PageRespDto.of(dto.getPageNum(), dto.getPageSize(), page.getTotal(),
                bookInfoPage.getRecords().stream().map(v -> BookInfoRespDto.builder()
                        .id(v.getId())
                        .bookName(v.getBookName())
                        .picUrl(v.getPicUrl())
                        .categoryName(v.getCategoryName())
                        .wordCount(v.getWordCount())
                        .visitCount(v.getVisitCount())
                        .updateTime(v.getUpdateTime())
                        .build()).toList()));
    }

    @Override
    public RestResp<PageRespDto<BookChapterRespDto>> listBookChapters(ChapterPageReqDto dto) {
        IPage<BookChapter> page = new Page<>();
        page.setCurrent(dto.getPageNum());
        page.setSize(dto.getPageSize());
        QueryWrapper<BookChapter> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.BookChapterTable.COLUMN_BOOK_ID, dto.getBookId())
                .orderByDesc(DatabaseConsts.BookChapterTable.COLUMN_CHAPTER_NUM);
        IPage<BookChapter> bookChapterPage = bookChapterMapper.selectPage(page, queryWrapper);
        return RestResp.ok(PageRespDto.of(dto.getPageNum(), dto.getPageSize(), page.getTotal(),
                bookChapterPage.getRecords().stream().map(v -> BookChapterRespDto.builder()
                        .id(v.getId())
                        .chapterName(v.getChapterName())
                        .chapterUpdateTime(v.getUpdateTime())
                        .isVip(v.getIsVip())
                        .build()).toList()));
    }

    @Override
    public RestResp<List<BookEsRespDto>> listNextEsBooks(Long maxBookId) {
        QueryWrapper<BookInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.clear();
        queryWrapper
                .orderByAsc(DatabaseConsts.CommonColumnEnum.ID.getName())
                .gt(DatabaseConsts.CommonColumnEnum.ID.getName(), maxBookId)
                .gt(DatabaseConsts.BookTable.COLUMN_WORD_COUNT, 0)
                .last(DatabaseConsts.SqlEnum.LIMIT_30.getSql());
        return RestResp.ok(bookInfoMapper.selectList(queryWrapper).stream().map(bookInfo -> BookEsRespDto.builder()
                .id(bookInfo.getId())
                .categoryId(bookInfo.getCategoryId())
                .categoryName(bookInfo.getCategoryName())
                .bookDesc(bookInfo.getBookDesc())
                .bookName(bookInfo.getBookName())
                .authorId(bookInfo.getAuthorId())
                .authorName(bookInfo.getAuthorName())
                .bookStatus(bookInfo.getBookStatus())
                .commentCount(bookInfo.getCommentCount())
                .isVip(bookInfo.getIsVip())
                .score(bookInfo.getScore())
                .visitCount(bookInfo.getVisitCount())
                .wordCount(bookInfo.getWordCount())
                .workDirection(bookInfo.getWorkDirection())
                .lastChapterId(bookInfo.getLastChapterId())
                .lastChapterName(bookInfo.getLastChapterName())
                .lastChapterUpdateTime(bookInfo.getLastChapterUpdateTime()
                        .toInstant(ZoneOffset.ofHours(8)).toEpochMilli())
                .build()).collect(Collectors.toList()));
    }

    @Override
    public RestResp<List<BookInfoRespDto>> listBookInfoByIds(List<Long> bookIds) {
        QueryWrapper<BookInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.in(DatabaseConsts.CommonColumnEnum.ID.getName(), bookIds);
        return RestResp.ok(
                bookInfoMapper.selectList(queryWrapper).stream().map(v -> BookInfoRespDto.builder()
                        .id(v.getId())
                        .bookName(v.getBookName())
                        .authorName(v.getAuthorName())
                        .picUrl(v.getPicUrl())
                        .bookDesc(v.getBookDesc())
                        .build()).collect(Collectors.toList()));
    }

    @Override
    public RestResp<BookContentAboutRespDto> getBookContentAbout(Long chapterId) {
        log.debug("userId:{}", UserHolder.getUserId());
        // 查询章节信息
        BookChapterRespDto bookChapter = bookChapterCacheManager.getChapter(chapterId);

        // 查询章节内容
        String content = bookContentCacheManager.getBookContent(chapterId);

        // 查询小说信息
        BookInfoRespDto bookInfo = bookInfoCacheManager.getBookInfo(bookChapter.getBookId());

        // 组装数据并返回
        return RestResp.ok(BookContentAboutRespDto.builder()
                .bookInfo(bookInfo)
                .chapterInfo(bookChapter)
                .bookContent(content)
                .build());
    }


    @Override
    public RestResp<UpdateBookChapterRspDto> getChapter(Long chapterId) {

        // 小说章节
        BookChapter bookChapter = bookChapterMapper.selectOne(new LambdaQueryWrapper<BookChapter>().eq(BookChapter::getId, chapterId));

        // 小说内容
        BookContent bookContent = bookContentMapper.selectOne(new LambdaQueryWrapper<BookContent>()
                .eq(BookContent::getChapterId, chapterId));

        // 小说信息
        BookInfo bookInfo = bookInfoMapper.selectOne(new LambdaQueryWrapper<BookInfo>()
                .eq(BookInfo::getId, bookChapter.getBookId()));


        return RestResp.ok(UpdateBookChapterRspDto.builder()
                .chapterContent(bookContent.getContent())
                .chapterName(bookChapter.getChapterName())
                .isVip(bookInfo.getIsVip()).build());
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public RestResp<Void> updateBookChapter(Long chapterId, UpdateBookChapterReqDto updateBookChapterReqDto) {
        // 先获得小说章节
        BookChapter bookChapter = bookChapterMapper.selectOne(new LambdaQueryWrapper<BookChapter>()
                .eq(BookChapter::getId, chapterId));
        // 查小说内容表
        BookContent bookContent = bookContentMapper.selectOne(new LambdaQueryWrapper<BookContent>()
                .eq(BookContent::getChapterId, chapterId));
        // 查小说信息表
        BookInfo bookInfo = bookInfoMapper.selectOne(new LambdaQueryWrapper<BookInfo>()
                .eq(BookInfo::getId, bookChapter.getBookId()));

        // 更新小说内容
        bookContent.setContent(updateBookChapterReqDto.getChapterContent());
        bookContent.setUpdateTime(LocalDateTime.now());
        bookContentMapper.updateById(bookContent);
        // 更新小说章节
        bookChapter.setChapterName(updateBookChapterReqDto.getChapterName());
        bookChapter.setUpdateTime(LocalDateTime.now());
        bookChapter.setIsVip(updateBookChapterReqDto.getIsVip());
        bookChapterMapper.updateById(bookChapter);
        // 更新小说信息
        bookInfo.setUpdateTime(LocalDateTime.now());
        // 如果此章节是小说的最新章节 则需要更新最后更新时间
        // 最新章节
        BookChapter newestChapter = bookChapterMapper.selectOne(new LambdaQueryWrapper<BookChapter>()
                .eq(BookChapter::getBookId, bookInfo.getId())
                .orderByDesc(BookChapter::getId)
                .last(DatabaseConsts.SqlEnum.LIMIT_1.getSql()));
        // 若id相同 说明是最后一章
        if (chapterId.equals(newestChapter.getId())){
            // 小说信息中的最新章节更新时间为现在
            bookInfo.setLastChapterUpdateTime(LocalDateTime.now());
        }
        bookInfoMapper.updateById(bookInfo);

        return null;
    }


    /**
     * 小说章节删除接口
     * @param chapterId
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteBookChapter(Long chapterId) {

        try {
            // 更新小说信息表的字数统计
            BookChapter bookChapter = bookChapterMapper.selectOne(new LambdaQueryWrapper<BookChapter>()
                    .eq(BookChapter::getId, chapterId));
            BookInfo bookInfo = bookInfoMapper.selectOne(new LambdaQueryWrapper<BookInfo>()
                    .eq(BookInfo::getId, bookChapter.getBookId()));

            bookInfo.setWordCount(bookInfo.getWordCount() - bookChapter.getWordCount());
            bookInfoMapper.updateById(bookInfo);

            // 删除章节表跟内容表与此章节有关的内容
            bookChapterMapper.deleteById(chapterId);
            bookContentMapper.delete(new LambdaQueryWrapper<BookContent>()
                    .eq(BookContent::getChapterId,chapterId));


            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public BookEsRespDto getEsBookById(Long bookId) {
        QueryWrapper<BookInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.clear();
        queryWrapper
                .eq(DatabaseConsts.CommonColumnEnum.ID.getName(), bookId)
                .gt(DatabaseConsts.BookTable.COLUMN_WORD_COUNT, 0);
        BookInfo bookInfo = bookInfoMapper.selectOne(queryWrapper);

        return BookEsRespDto.builder()
                .id(bookInfo.getId())
                .categoryId(bookInfo.getCategoryId())
                .categoryName(bookInfo.getCategoryName())
                .bookDesc(bookInfo.getBookDesc())
                .bookName(bookInfo.getBookName())
                .authorId(bookInfo.getAuthorId())
                .authorName(bookInfo.getAuthorName())
                .bookStatus(bookInfo.getBookStatus())
                .commentCount(bookInfo.getCommentCount())
                .isVip(bookInfo.getIsVip())
                .score(bookInfo.getScore())
                .visitCount(bookInfo.getVisitCount())
                .wordCount(bookInfo.getWordCount())
                .workDirection(bookInfo.getWorkDirection())
                .lastChapterId(bookInfo.getLastChapterId())
                .lastChapterName(bookInfo.getLastChapterName())
                .lastChapterUpdateTime(bookInfo.getLastChapterUpdateTime()
                        .toInstant(ZoneOffset.ofHours(8)).toEpochMilli())
                .build();
    }

    @Override
    public RestResp<List<BookInfoRespDto>> listBookByHot() {
        LinkedList<BookInfoRespDto> result = new LinkedList<>();
        // 查询评分前四作为轮播图
        List<BookInfo> carouselBookInfos = bookInfoMapper.selectList(new LambdaQueryWrapper<BookInfo>()
                .orderByDesc(BookInfo::getScore)
                .last("limit 4"));
        result.addAll(getReturnList(carouselBookInfos));
        // 查询评分前10,点击前10作为顶部栏
        List<BookInfo> topBookInfos = bookInfoMapper.selectList(new LambdaQueryWrapper<BookInfo>()
                .orderByDesc(BookInfo::getScore)
                .orderByDesc(BookInfo::getVisitCount)
                .last("limit 10"));
        result.addAll(getReturnList(topBookInfos));
        // 最近更新前5作为本周强推
        List<BookInfo> weekBookInfos = bookInfoMapper.selectList(new LambdaQueryWrapper<BookInfo>()
                .orderByDesc(BookInfo::getLastChapterUpdateTime)
                .last("limit 5"));
        result.addAll(getReturnList(weekBookInfos));
        // 查询点击前6作为热门推荐
        List<BookInfo> hotBookInfos = bookInfoMapper.selectList(new LambdaQueryWrapper<BookInfo>()
                .orderByDesc(BookInfo::getVisitCount)
                .last("limit 6"));
        result.addAll(getReturnList(hotBookInfos));
        // 总字数前6作为精品推荐
        List<BookInfo> goodBookInfos = bookInfoMapper.selectList(new LambdaQueryWrapper<BookInfo>()
                .orderByDesc(BookInfo::getWordCount)
                .last("limit 6"));
        result.addAll(getReturnList(goodBookInfos));
        return RestResp.ok(result);
    }

    private static List<BookInfoRespDto> getReturnList(List<BookInfo> bookInfos) {
        return bookInfos.stream().map(f -> {
            BookInfoRespDto bookInfoRespDto = new BookInfoRespDto();
            BeanUtils.copyProperties(f, bookInfoRespDto);
            return bookInfoRespDto;
        }).toList();
    }
}
