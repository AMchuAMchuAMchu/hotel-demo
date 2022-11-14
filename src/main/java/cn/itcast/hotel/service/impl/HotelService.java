package cn.itcast.hotel.service.impl;

import cn.itcast.hotel.mapper.HotelMapper;
import cn.itcast.hotel.pojo.Hotel;
import cn.itcast.hotel.pojo.HotelDoc;
import cn.itcast.hotel.pojo.PageResult;
import cn.itcast.hotel.pojo.RequestParams;
import cn.itcast.hotel.service.IHotelService;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
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
    public PageResult search(RequestParams requestParams) {
        try {
            //1.0 准备Request
            SearchRequest request = new SearchRequest("hotel");

            buildBasicQuery(requestParams, request);
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

    private void buildBasicQuery(RequestParams params, SearchRequest request) {
        //2.0 准备DSL
        String key = params.getKey();
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        //3.0 发送请求,得到响应
        if (StringUtils.isNotBlank(key)) {
            // 不为空，根据关键字查询
            boolQuery.must(QueryBuilders.matchQuery("all", key));
        } else {
            // 为空，查询所有
            boolQuery.must(QueryBuilders.matchAllQuery());
        }

        // 1.2.品牌
        String brand = params.getBrand();
        if (StringUtils.isNotBlank(brand)) {
            boolQuery.filter(QueryBuilders.termQuery("brand", brand));
        }
        // 1.3.城市
        String city = params.getCity();
        if (StringUtils.isNotBlank(city)) {
            boolQuery.filter(QueryBuilders.termQuery("city", city));
        }
        // 1.4.星级
        String starName = params.getStarName();
        if (StringUtils.isNotBlank(starName)) {
            boolQuery.filter(QueryBuilders.termQuery("starName", starName));
        }
        // 1.5.价格范围
        Integer minPrice = params.getMinPrice();
        Integer maxPrice = params.getMaxPrice();
        if (minPrice != null && maxPrice != null) {
            maxPrice = maxPrice == 0 ? Integer.MAX_VALUE : maxPrice;
            boolQuery.filter(QueryBuilders.rangeQuery("price").gte(minPrice).lte(maxPrice));
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
            System.out.println(hotelDoc);
        }
        return new PageResult(total, hotelDocs);
    }
}
