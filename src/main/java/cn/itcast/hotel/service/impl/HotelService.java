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
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

@Service
public class HotelService extends ServiceImpl<HotelMapper, Hotel> implements IHotelService {


    @Autowired
    private RestHighLevelClient rhlc;

    @Override
    public PageResult search(RequestParams requestParams) {
        try {
            //1.0 准备Request
            SearchRequest request = new SearchRequest("hotel");

            //2.0 准备DSL
            String key = requestParams.getKey();
            //3.0 发送请求,得到响应
            if (key == null || "".equals(key)) {
                request.source().query(QueryBuilders.matchAllQuery());
            } else {
                request.source().query(QueryBuilders.matchQuery("all", key));
            }
            int page = requestParams.getPage();
            int size = requestParams.getSize();
            request.source().from((page - 1) * size).size(size);
            //4.0 解析响应
            SearchResponse search = rhlc.search(request, RequestOptions.DEFAULT);
            return handleResponse(search);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private PageResult handleResponse(SearchResponse search) {
        SearchHits searchHits = search.getHits();

        long total = searchHits.getTotalHits().value;

        SearchHit[] hits = searchHits.getHits();

        System.out.println("一共搜索到了:" + total + "条数据.O(∩_∩)O哈哈~");


        ArrayList<HotelDoc> hotelDocs = new ArrayList<>();
        for (SearchHit hit : hits) {
            String sourceAsString = hit.getSourceAsString();
            HotelDoc hotelDoc = JSON.parseObject(sourceAsString, HotelDoc.class);
            hotelDocs.add(hotelDoc);
        }
        return new PageResult(total,hotelDocs);


    }
}
