package cn.itcast.hotel;

import cn.itcast.hotel.pojo.HotelDoc;
import com.alibaba.fastjson.JSON;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.Map;

/**
 * Description ==> TODO
 * BelongsProject ==> hotel-demo
 * BelongsPackage ==> cn.itcast.hotel
 * Version ==> 1.0
 * CreateTime ==> 2022-11-12 17:52:02
 * Author ==> _02雪乃赤瞳楪祈校条祭_艾米丽可锦木千束木更七草荠_制作委员会_start
 */
@SpringBootTest
public class HotelTestRestClient {


    @Autowired
    private RestHighLevelClient rhlc;


    @Test
    void testHighlight() throws IOException {

        SearchRequest searchRequest = new SearchRequest("hotel");
        searchRequest.source().query(QueryBuilders.matchQuery("all","如家"));
        searchRequest.source().highlighter(new HighlightBuilder().field("name").requireFieldMatch(false));
        SearchResponse search = rhlc.search(searchRequest, RequestOptions.DEFAULT);

        handleResponse(search);
    }


    @Test
    void testSortPage() throws IOException {

        int page = 1, size = 5;
        SearchRequest searchRequest = new SearchRequest("hotel");
        searchRequest.source().query(QueryBuilders.matchAllQuery());
        searchRequest.source().sort("price", SortOrder.ASC);
        searchRequest.source().from((page - 1) * size).size(size);
        SearchResponse searchResponse = rhlc.search(searchRequest, RequestOptions.DEFAULT);
        handleResponse(searchResponse);


    }


    @Test
    void testTerm() throws IOException {

        SearchRequest searchRequest = new SearchRequest("hotel");

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

//        boolQuery.must(QueryBuilders.termQuery("city","杭州"));
        boolQuery.must(QueryBuilders.termQuery("city", "上海"));

        boolQuery.filter(QueryBuilders.rangeQuery("price").lte(250));

        searchRequest.source().query(boolQuery);

        SearchResponse searchResponse = rhlc.search(searchRequest, RequestOptions.DEFAULT);

        handleResponse(searchResponse);


    }


    @Test
    void testMatchAll() throws IOException {

        SearchRequest searchRequest = new SearchRequest("hotel");

        searchRequest.source().query(QueryBuilders.matchQuery("brand", "如家"));


        SearchResponse search = rhlc.search(searchRequest, RequestOptions.DEFAULT);

        //处理结果的方法
        handleResponse(search);

    }

    private void handleResponse(SearchResponse search) {
        SearchHits searchHits = search.getHits();

        long value = searchHits.getTotalHits().value;

        SearchHit[] hits = searchHits.getHits();

        System.out.println("一共搜索到了:" + value + "条数据.O(∩_∩)O哈哈~");


        for (SearchHit hit : hits) {
//            System.out.println(hit);
            String sourceAsString = hit.getSourceAsString();
//            System.out.println(sourceAsString);
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            String s = null;
            if (!CollectionUtils.isEmpty(highlightFields)){
                HighlightField name = highlightFields.get("name");
                if (name != null){
                    s = name.getFragments()[0].toString();
                }
            }

            HotelDoc hotelDoc = JSON.parseObject(sourceAsString, HotelDoc.class);
            hotelDoc.setName(s);
            System.out.println(hotelDoc);
        }
    }

    @AfterEach
    public void close() throws IOException {
        rhlc.close();
    }

}
