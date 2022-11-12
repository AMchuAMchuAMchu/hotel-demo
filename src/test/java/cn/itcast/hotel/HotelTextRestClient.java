package cn.itcast.hotel;

import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

/**
 * Description ==> TODO
 * BelongsProject ==> hotel-demo
 * BelongsPackage ==> cn.itcast.hotel
 * Version ==> 1.0
 * CreateTime ==> 2022-11-12 17:52:02
 * Author ==> _02雪乃赤瞳楪祈校条祭_艾米丽可锦木千束木更七草荠_制作委员会_start
 */
@SpringBootTest
public class HotelTextRestClient {


    @Autowired
    private RestHighLevelClient rhlc;

    @Test
    void testMatchAll() throws IOException {

        SearchRequest searchRequest = new SearchRequest("hotel");

        searchRequest.source().query(QueryBuilders.matchQuery("brand","如家"));

        SearchResponse search = rhlc.search(searchRequest, RequestOptions.DEFAULT);

        SearchHit[] hits = search.getHits().getHits();

        long value = search.getHits().getTotalHits().value;

        System.out.println("一共搜索到了:"+value+"条数据.O(∩_∩)O哈哈~");


        for (SearchHit hit : hits) {
//            System.out.println(hit);
            String sourceAsString = hit.getSourceAsString();
            System.out.println(sourceAsString);
        }

    }

    @AfterEach
    public void close() throws IOException {
        rhlc.close();
    }

}
