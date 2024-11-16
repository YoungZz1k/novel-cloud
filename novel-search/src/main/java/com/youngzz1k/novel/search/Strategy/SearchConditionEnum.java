package com.youngzz1k.novel.search.Strategy;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import co.elastic.clients.json.JsonData;
import com.youngzz1k.novel.book.dto.req.BookSearchReqDto;
import com.youngzz1k.novel.search.constant.EsConsts;


public enum SearchConditionEnum {

    /**
     * 关键词匹配
     */
    KeywordMatchStrategy {
        @Override
        public void buildQuery(BoolQuery.Builder b, BookSearchReqDto condition) {
            b.must(f -> f.multiMatch(t -> t
                    .fields(EsConsts.BookIndex.FIELD_BOOK_NAME + "^2",
                            EsConsts.BookIndex.FIELD_AUTHOR_NAME + "^1.8",
                            EsConsts.BookIndex.FIELD_BOOK_DESC + "^0.1")
                    .query(condition.getKeyword())));
        }
    },
    /**
     *  精确查询
     */
    WorkDirectionMatchStrategy {
        @Override
        public void buildQuery(BoolQuery.Builder b, BookSearchReqDto condition) {
            b.must(TermQuery.of(f -> f
                            .field(EsConsts.BookIndex.FIELD_WORK_DIRECTION)
                            .value(condition.getWorkDirection())
                    )._toQuery()
            );
        }
    },

    /**
     * 小说类别
     */
    CategoryIdMatchStrategy {
        @Override
        public void buildQuery(BoolQuery.Builder b, BookSearchReqDto condition) {
            b.must(TermQuery.of(f -> f
                            .field(EsConsts.BookIndex.FIELD_CATEGORY_ID)
                            .value(condition.getCategoryId())
                    )._toQuery()
            );
        }
    },

    /**
     * 完结状态
     */
    BookStatusMatchStrategy {
        @Override
        public void buildQuery(BoolQuery.Builder b, BookSearchReqDto condition) {
            b.must(TermQuery.of(f -> f
                            .field(EsConsts.BookIndex.FIELD_BOOK_STATUS)
                            .value(condition.getBookStatus())
                    )._toQuery()
            );
        }
    },

    /**
     * 大于最小字数
     */
    WordCountMinMatchStrategy {
        @Override
        public void buildQuery(BoolQuery.Builder b, BookSearchReqDto condition) {
            b.must(RangeQuery.of(f -> f
                            .field(EsConsts.BookIndex.FIELD_WORD_COUNT)
                            .gte(JsonData.of(condition.getWordCountMin()))
                    )._toQuery()
            );
        }
    },

    /**
     * 小于最大字数
     */
    WordCountMaxMatchStrategy {
        @Override
        public void buildQuery(BoolQuery.Builder b, BookSearchReqDto condition) {
            b.must(RangeQuery.of(f -> f
                            .field(EsConsts.BookIndex.FIELD_WORD_COUNT)
                            .lt(JsonData.of(condition.getWordCountMax()))
                    )._toQuery()
            );
        }
    },

    /**
     * 大于最小更新日期
     */
    UpdateTimeMinMatchStrategy {
        @Override
        public void buildQuery(BoolQuery.Builder b, BookSearchReqDto condition) {
            b.must(RangeQuery.of(f -> f
                            .field(EsConsts.BookIndex.FIELD_LAST_CHAPTER_UPDATE_TIME)
                            .gte(JsonData.of(condition.getUpdateTimeMin().getTime()))
                    )._toQuery()
            );
        }
    };

    public abstract void buildQuery(BoolQuery.Builder b, BookSearchReqDto condition);
}
