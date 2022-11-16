package cn.itcast.hotel;

import cn.itcast.hotel.service.IHotelService;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Description ==> TODO
 * BelongsProject ==> hotel-demo
 * BelongsPackage ==> cn.itcast.hotel
 * Version ==> 1.0
 * CreateTime ==> 2022-11-16 19:41:36
 * Author ==> _02雪乃赤瞳楪祈校条祭_艾米丽可锦木千束木更七草荠_制作委员会_start
 */
@SpringBootTest
public class AggTest {

    @Autowired
    private RestHighLevelClient request;

    @Autowired
    private IHotelService iHotelService;

    @Test
    void testAggHeima(){

        Map<String, List<String>> filters = iHotelService.filters();

        System.out.println(filters);


    }

    @Test
    void testAggBucket() throws IOException {


        SearchRequest searchRequest = new SearchRequest("hotel");
        searchRequest.source().size(0);
        searchRequest.source().aggregation(AggregationBuilders.terms("brandAgg").field("brand").size(20));
        SearchResponse search = request.search(searchRequest, RequestOptions.DEFAULT);

        Aggregations aggregations = search.getAggregations();

        Terms brandTerms = aggregations.get("brandAgg");

        List<? extends Terms.Bucket> buckets = brandTerms.getBuckets();

        buckets.forEach((item)->{
            String s = item.getKey().toString();
            System.out.println(s);
        });


    }


}
