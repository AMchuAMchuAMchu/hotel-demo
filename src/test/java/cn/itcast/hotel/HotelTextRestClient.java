package cn.itcast.hotel;

import cn.itcast.hotel.pojo.HotelDoc;
import com.alibaba.fastjson.JSON;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
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
    void testTerm() throws IOException {

        SearchRequest searchRequest = new SearchRequest("hotel");

        searchRequest.source().query(QueryBuilders.matchQuery("city","杭州"));

        SearchResponse searchResponse = rhlc.search(searchRequest, RequestOptions.DEFAULT);

        handleResponse(searchResponse);


    }


    @Test
    void testMatchAll() throws IOException {

        SearchRequest searchRequest = new SearchRequest("hotel");

        searchRequest.source().query(QueryBuilders.matchQuery("brand","如家"));



        SearchResponse search = rhlc.search(searchRequest, RequestOptions.DEFAULT);

        //处理结果的方法
        handleResponse(search);

    }

    private void handleResponse(SearchResponse search) {
        SearchHits searchHits = search.getHits();

        long value = searchHits.getTotalHits().value;

        SearchHit[] hits = searchHits.getHits();

        System.out.println("一共搜索到了:"+value+"条数据.O(∩_∩)O哈哈~");


        for (SearchHit hit : hits) {
//            System.out.println(hit);
            String sourceAsString = hit.getSourceAsString();
//            System.out.println(sourceAsString);
            HotelDoc hotelDoc = JSON.parseObject(sourceAsString, HotelDoc.class);
            System.out.println(hotelDoc);
        }
    }

    @AfterEach
    public void close() throws IOException {
        rhlc.close();
    }

}
