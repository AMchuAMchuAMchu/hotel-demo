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
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class HotelService extends ServiceImpl<HotelMapper, Hotel> implements IHotelService {


    @Autowired
    private RestHighLevelClient rhlc;

    @Override
    public PageResult search(RequestParams params) {

        try {
            SearchRequest searchRequest = new SearchRequest("hotel");

            buildBasicQuery(params, searchRequest);

            Integer page = params.getPage();

            Integer size = params.getSize();

            searchRequest.source().from((page - 1) * size).size(size);


            String location = params.getLocation();


            if (location!=null&&!location.equals("")){
                searchRequest.source()
                        .sort(SortBuilders.geoDistanceSort("location",new GeoPoint(location))
                                .order(SortOrder.ASC)
                                .unit(DistanceUnit.KILOMETERS));
            }

            SearchResponse searchResponse = null;
            searchResponse = rhlc.search(searchRequest, RequestOptions.DEFAULT);

            return getPageResult(searchResponse);
        } catch (IOException e) {
            throw new RuntimeException("搜索失败!",e);
        }

    }

    private void buildBasicQuery(RequestParams params, SearchRequest searchRequest) {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        String key = params.getKey();
        if (StringUtils.isNotBlank(key)) {
            boolQuery.must(QueryBuilders.matchQuery("all", key));
        }else {
            boolQuery.must(QueryBuilders.matchAllQuery());
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

        FunctionScoreQueryBuilder isAD = QueryBuilders.functionScoreQuery(boolQuery, new FunctionScoreQueryBuilder.FilterFunctionBuilder[]{
                new FunctionScoreQueryBuilder.FilterFunctionBuilder(QueryBuilders.termQuery("isAD", true)
                        ,ScoreFunctionBuilders.weightFactorFunction(10))
        });

        searchRequest.source().query(isAD);
    }

    @Override
    public Map<String, List<String>> filters(RequestParams requestParams) {
        HashMap<String, List<String>> bucketHashes = null;
        try {
            SearchRequest searchRequest = new SearchRequest("hotel");
            buildBasicQuery(requestParams,searchRequest);
            searchRequest.source().size(0);
            buildAggregation(searchRequest);
            SearchResponse search = null;
            search = rhlc.search(searchRequest, RequestOptions.DEFAULT);
            bucketHashes = new HashMap<>();
            Aggregations aggregations = search.getAggregations();

            ArrayList<String> brandList = getAggByName(aggregations,"brandAgg");
            bucketHashes.put("品牌",brandList);

            ArrayList<String> cityList = getAggByName(aggregations,"cityAgg");
            bucketHashes.put("城市",cityList);

            ArrayList<String> starList = getAggByName(aggregations,"starAgg");
            bucketHashes.put("星级",starList);
        } catch (IOException e) {
            throw new RuntimeException("搜索失败!",e);
        }
        return bucketHashes;
    }

    private ArrayList<String> getAggByName(Aggregations aggregations,String aggName) {
        ArrayList<String> brandList = new ArrayList<>();
        Terms brandTerms = aggregations.get(aggName);
        List<? extends Terms.Bucket> buckets = brandTerms.getBuckets();
        buckets.forEach((item)->{
            String s = item.getKey().toString();
            brandList.add(s);
        });
        return brandList;
    }

    private void buildAggregation(SearchRequest searchRequest) {
        searchRequest.source().aggregation(AggregationBuilders.terms("brandAgg").field("brand").size(100));
        searchRequest.source().aggregation(AggregationBuilders.terms("cityAgg").field("city").size(100));
        searchRequest.source().aggregation(AggregationBuilders.terms("starAgg").field("starName").size(100));
    }

    private PageResult getPageResult(SearchResponse searchResponse) {
        SearchHits responseHits = searchResponse.getHits();

        long totalHits = responseHits.getTotalHits().value;

        SearchHit[] searchHits = responseHits.getHits();

        ArrayList<HotelDoc> hotelDocs = new ArrayList<>();

        for (SearchHit searchHit : searchHits) {
            String sourceAsString = searchHit.getSourceAsString();

            HotelDoc hotel = JSON.parseObject(sourceAsString, HotelDoc.class);

            Object[] sortValues = searchHit.getSortValues();

            if (sortValues.length>0){
                Object sortValue = sortValues[0];
                hotel.setDistance(sortValue);
            }

            hotelDocs.add(hotel);
        }

        return new PageResult(totalHits, hotelDocs);
    }
}
