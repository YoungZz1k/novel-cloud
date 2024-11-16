package com.youngzz1k.novel.search.consumer;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Time;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import com.youngzz1k.novel.book.dto.resp.BookEsRespDto;
import com.youngzz1k.novel.common.constant.AmqpConsts;
import com.youngzz1k.novel.common.resp.RestResp;
import com.youngzz1k.novel.search.constant.EsConsts;
import com.youngzz1k.novel.search.manager.feign.BookFeignManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SaveChapterToEsConsumer {

    @Autowired
    private BookFeignManager bookFeignManager;

    @Autowired
    private final ElasticsearchClient elasticsearchClient;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = AmqpConsts.BookChangeMq.QUEUE_ES_UPDATE),
            exchange = @Exchange(value = AmqpConsts.BookChangeMq.EXCHANGE_NAME,type = "fanout")
    ))
    private void listeningSaveChapter(Long bookId) { // 监听方法返回值必须为void

        // 调用bookFeign获取更新的小说
        RestResp<BookEsRespDto> resp = bookFeignManager.getEsBookById(bookId);
        BookEsRespDto book = resp.getData();

        try {
            // 更新到ES
            if (book != null) {

                BulkRequest.Builder br = new BulkRequest.Builder();
                br.operations(op -> op
                        .index(idx -> idx
                                .index(EsConsts.BookIndex.INDEX_NAME)
                                .id(book.getId().toString())
                                .document(book)
                        )
                ).timeout(Time.of(t -> t.time("10s")));
                BulkResponse result = elasticsearchClient.bulk(br.build());

                // Log errors, if any
                if (result.errors()) {
                    log.error("Bulk had errors");
                    for (BulkResponseItem item : result.items()) {
                        if (item.error() != null) {
                            log.error(item.error().reason());
                        }
                    }
                }
            }
        }catch (Exception e){
            log.error(e.getMessage(), e);
        }
    }

}
