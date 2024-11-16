package io.github.xxyopen.novel.search.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import co.elastic.clients.json.JsonData;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import io.github.xxyopen.novel.book.dto.req.BookSearchReqDto;
import io.github.xxyopen.novel.book.dto.resp.BookEsRespDto;
import io.github.xxyopen.novel.book.dto.resp.BookInfoRespDto;
import io.github.xxyopen.novel.common.resp.PageRespDto;
import io.github.xxyopen.novel.common.resp.RestResp;
import io.github.xxyopen.novel.search.Strategy.SearchConditionEnum;
import io.github.xxyopen.novel.search.constant.EsConsts;
import io.github.xxyopen.novel.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Elasticsearch 搜索 服务实现类
 *
 * @author YoungZz1k
 * @date 2024/11/23
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SearchServiceImpl implements SearchService {

    private final ElasticsearchClient esClient;

    @SneakyThrows
    @Override
    public RestResp<PageRespDto<BookInfoRespDto>> searchBooks(BookSearchReqDto condition) {

        SearchResponse<BookEsRespDto> response = esClient.search(s -> {

                    SearchRequest.Builder searchBuilder = s.index(EsConsts.BookIndex.INDEX_NAME);

                    // 构造搜索条件
                    buildSearchCondition(condition, searchBuilder);

                    // 排序
                    if (StringUtils.isNotEmpty(condition.getSort())) {
                        searchBuilder.sort(f -> f.field(q -> q
                                .field(StringUtils.underlineToCamel(condition.getSort().split(" ")[0]))
                                .order(SortOrder.Desc))
                        );
                    }

                    // 分页
                    searchBuilder.from((condition.getPageNum() - 1) * condition.getPageSize())
                            .size(condition.getPageSize());

                    // 高亮
                    searchBuilder.highlight(
                            h -> h.fields(EsConsts.BookIndex.FIELD_BOOK_NAME, t -> t.preTags("<em style = 'color:red' >").postTags("</em>"))
                                    .fields(EsConsts.BookIndex.FIELD_AUTHOR_NAME, t -> t.preTags("<em style = 'color:red' >").postTags("</em>"))
                    );

                    return searchBuilder;
                },
                BookEsRespDto.class
        );

        TotalHits total = response.hits().total();

        List<BookInfoRespDto> list = new ArrayList<>();
        List<Hit<BookEsRespDto>> hits = response.hits().hits();
        // 类型推断 var 非常适合 for 循环，JDK 10 引入，JDK 11 改进
        for (var hit : hits) {
            BookEsRespDto book = hit.source();
            assert book != null;
            if (!CollectionUtils.isEmpty(hit.highlight().get(EsConsts.BookIndex.FIELD_BOOK_NAME))) {
                book.setBookName(hit.highlight().get(EsConsts.BookIndex.FIELD_BOOK_NAME).get(0));
            }
            if (!CollectionUtils.isEmpty(
                    hit.highlight().get(EsConsts.BookIndex.FIELD_AUTHOR_NAME))) {
                book.setAuthorName(
                        hit.highlight().get(EsConsts.BookIndex.FIELD_AUTHOR_NAME).get(0));
            }
            list.add(BookInfoRespDto.builder()
                    .id(book.getId())
                    .bookName(book.getBookName())
                    .categoryId(book.getCategoryId())
                    .categoryName(book.getCategoryName())
                    .authorId(book.getAuthorId())
                    .authorName(book.getAuthorName())
                    .wordCount(book.getWordCount())
                    .lastChapterName(book.getLastChapterName())
                    .build());
        }
        assert total != null;
        return RestResp.ok(
                PageRespDto.of(condition.getPageNum(), condition.getPageSize(), total.value(), list));

    }

    /**
     * 构建检索条件
     */
    private void buildSearchCondition(BookSearchReqDto condition, SearchRequest.Builder searchBuilder) {

        BoolQuery boolQuery = BoolQuery.of(b -> {

            // 只查字数大于0的
            b.must(RangeQuery.of(f -> f
                            .field(EsConsts.BookIndex.FIELD_WORD_COUNT)
                            .gt(JsonData.of(0)))
                    ._toQuery()
            );

            if (StringUtils.isNotEmpty(condition.getKeyword())) {
                // 关键词匹配
                SearchConditionEnum.KeywordMatchStrategy.buildQuery(b,condition);
            }

            // 精确查询
            if (Objects.nonNull(condition.getWorkDirection())) {
                SearchConditionEnum.WorkDirectionMatchStrategy.buildQuery(b,condition);
            }

            // 小说类别
            if (Objects.nonNull(condition.getCategoryId())) {
                SearchConditionEnum.CategoryIdMatchStrategy.buildQuery(b,condition);
            }

            // 完结状态
            if (Objects.nonNull(condition.getBookStatus())) {
                SearchConditionEnum.BookStatusMatchStrategy.buildQuery(b,condition);
            }

            // 范围查询
            if (Objects.nonNull(condition.getWordCountMin())) {// 大于最小字数
                SearchConditionEnum.WordCountMinMatchStrategy.buildQuery(b,condition);
            }

            if (Objects.nonNull(condition.getWordCountMax())) {// 小于最大字数
                SearchConditionEnum.WordCountMaxMatchStrategy.buildQuery(b,condition);
            }

            if (Objects.nonNull(condition.getUpdateTimeMin())) { // 大于最小更新日期
                SearchConditionEnum.UpdateTimeMinMatchStrategy.buildQuery(b,condition);
            }


            return b;
        });

        searchBuilder.query(q -> q.bool(boolQuery));

    }
}
