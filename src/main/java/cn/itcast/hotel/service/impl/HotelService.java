package cn.itcast.hotel.service.impl;

import cn.itcast.hotel.mapper.HotelMapper;
import cn.itcast.hotel.pojo.Hotel;
import cn.itcast.hotel.pojo.HotelDoc;
import cn.itcast.hotel.pojo.PageResult;
import cn.itcast.hotel.pojo.RequestParams;
import cn.itcast.hotel.service.IHotelService;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;


@Service
public class HotelService extends ServiceImpl<HotelMapper, Hotel> implements IHotelService {


    @Autowired
    private RestHighLevelClient rhlc;

    @Override
    public PageResult search(RequestParams params) {

        SearchRequest searchRequest = new SearchRequest("hotel");

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        if (StringUtils.isNotBlank(params.getKey())) {
            boolQuery.must(QueryBuilders.matchQuery("all", params.getKey()));
        }
        if (params.getBrand() != null && !params.getBrand().equals("")) {
            boolQuery.must(QueryBuilders.termQuery("brand", params.getBrand()));
        }
        if (params.getCity() != null && !params.getCity().equals("")) {
            boolQuery.filter(QueryBuilders.termQuery("city", params.getCity()));
        }
        if (params.getStarName() != null && !params.getStarName().equals("")) {
            boolQuery.filter(QueryBuilders.termQuery("starName", params.getStarName()));
        }
        if (params.getMaxPrice() != null && params.getMinPrice() != null) {
            boolQuery.filter(QueryBuilders.rangeQuery("price").gte(params.getMinPrice()).lte(params.getMaxPrice()));
        }

        Integer page = params.getPage();

        Integer size = params.getSize();

        searchRequest.source().from((page - 1) * size).size(size);


        //记得放回query部分作为一个条件查询的说...
        searchRequest.source().query(boolQuery);

        SearchResponse searchResponse = null;
        try {
            searchResponse = rhlc.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return getPageResult(searchResponse);

    }

    private PageResult getPageResult(SearchResponse searchResponse) {
        SearchHits responseHits = searchResponse.getHits();

        long totalHits = responseHits.getTotalHits().value;

        SearchHit[] searchHits = responseHits.getHits();

        ArrayList<HotelDoc> hotelDocs = new ArrayList<>();

        for (SearchHit searchHit : searchHits) {
            String sourceAsString = searchHit.getSourceAsString();

            HotelDoc hotel = JSON.parseObject(sourceAsString, HotelDoc.class);

            hotelDocs.add(hotel);
        }

        return new PageResult(totalHits, hotelDocs);
    }
}
