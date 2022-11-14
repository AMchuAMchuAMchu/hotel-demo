package cn.itcast.hotel;

import cn.itcast.hotel.pojo.Hotel;
import cn.itcast.hotel.pojo.HotelDoc;
import cn.itcast.hotel.service.IHotelService;
import com.alibaba.fastjson.JSON;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;

import static cn.itcast.hotel.constants.HotelIndexConstants.MAPPING_TEMPLATE;

@SpringBootTest
class HotelIndexTest {

    @Autowired
    private RestHighLevelClient client;

    @Autowired
    private IHotelService iHotelService;



    @Test
    void testSearchAll02() throws IOException {

        SearchRequest searchRequest = new SearchRequest("hotel");

        searchRequest.source().query(QueryBuilders.matchAllQuery());

        SearchResponse search = client.search(searchRequest, RequestOptions.DEFAULT);

        SearchHit[] hits = search.getHits().getHits();

        for (SearchHit hit : hits) {
            System.out.println(hit);
        }

    }


    @Test
    void testUpdateTest02() throws IOException {

        UpdateRequest updateRequest = new UpdateRequest("hotel","38665");
        updateRequest.doc("starName","二钻");
        client.update(updateRequest,RequestOptions.DEFAULT);

    }


    @Test
    void testGetById() throws IOException {

        GetRequest getRequest = new GetRequest("hotel");
        getRequest.id("38665");
        GetResponse documentFields = client.get(getRequest, RequestOptions.DEFAULT);

        String sourceAsString = documentFields.getSourceAsString();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println(sourceAsString);

    }

    @Test
    void testInsertAll() throws IOException {

        BulkRequest bulkRequest = new BulkRequest();

        List<Hotel> list = iHotelService.list();

        list.forEach(item->{
            HotelDoc hotelDoc = new HotelDoc(item);
            bulkRequest.add(new IndexRequest("hotel").id(hotelDoc.getId().toString()).source(JSON.toJSONString(hotelDoc),XContentType.JSON));
        });
        client.bulk(bulkRequest,RequestOptions.DEFAULT);
    }

    @Test
    void testCreateIndex() throws IOException {
        // 1.准备Request      PUT /hotel
        CreateIndexRequest request = new CreateIndexRequest("hotel");
        // 2.准备请求参数
        request.source(MAPPING_TEMPLATE, XContentType.JSON);
        // 3.发送请求
        client.indices().create(request, RequestOptions.DEFAULT);
    }

    @Test
    void testExistsIndex() throws IOException {
        // 1.准备Request
        GetIndexRequest request = new GetIndexRequest("hotel");
        // 3.发送请求
        boolean isExists = client.indices().exists(request, RequestOptions.DEFAULT);

        System.out.println(isExists ? "存在" : "不存在");
    }
    @Test
    void testDeleteIndex() throws IOException {
        // 1.准备Request
        DeleteIndexRequest request = new DeleteIndexRequest("hotel");
        // 3.发送请求
        client.indices().delete(request, RequestOptions.DEFAULT);
    }




    @AfterEach
    void tearDown() throws IOException {
        client.close();
    }





}
