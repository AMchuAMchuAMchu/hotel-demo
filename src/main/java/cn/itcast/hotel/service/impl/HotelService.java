package cn.itcast.hotel.service.impl;

import cn.itcast.hotel.mapper.HotelMapper;
import cn.itcast.hotel.pojo.Hotel;
import cn.itcast.hotel.pojo.HotelDoc;
import cn.itcast.hotel.pojo.PageResult;
import cn.itcast.hotel.pojo.RequestParams;
import cn.itcast.hotel.service.IHotelService;
import com.alibaba.fastjson.JSON;
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
    public PageResult search(RequestParams params){

        SearchRequest searchRequest = new SearchRequest("hotel");

        searchRequest.source().query(QueryBuilders.matchAllQuery());

        SearchResponse searchResponse = null;
        try {
            searchResponse = rhlc.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        SearchHits responseHits = searchResponse.getHits();

        long totalHits = responseHits.getTotalHits().value;

        SearchHit[] searchHits = responseHits.getHits();

        ArrayList<HotelDoc> hotelDocs = new ArrayList<>();

        for (SearchHit searchHit : searchHits) {
            String sourceAsString = searchHit.getSourceAsString();

            Hotel hotel = JSON.parseObject(sourceAsString, Hotel.class);

            HotelDoc hotelDoc = new HotelDoc(hotel);

            hotelDocs.add(hotelDoc);

        }

        return new PageResult(totalHits,hotelDocs);
    }
}
