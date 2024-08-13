package com.xuecheng.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.search.dto.SearchCourseParamDto;
import com.xuecheng.search.dto.SearchPageResultDto;
import com.xuecheng.search.po.CourseIndex;
import com.xuecheng.search.service.CourseSearchService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author liujue
 */
@Slf4j
@Service
public class CourseSearchServiceImpl implements CourseSearchService {

    /**
     * 课程索引库名
     */
    @Value("${elasticsearch.course.index}")
    private String courseIndexName;

    /**
     * 索引字段
     */
    @Value("${elasticsearch.course.source_fields}")
    private String sourceFields;

    private final RestHighLevelClient restHighLevelClient;

    @Autowired
    public CourseSearchServiceImpl(RestHighLevelClient restHighLevelClient) {
        this.restHighLevelClient = restHighLevelClient;
    }

    /**
     * 搜索课程列表
     * 需要完成的业务包括:
     * 1. 整体采用布尔查询
     * 2. 根据关键字搜索，采用 MultiMatchQuery，搜索 name, description 字段
     * 3. 根据分类、课程等级搜索采用过滤器实现
     * 4. 分页查询
     * 5. 高亮显示
     * 6. 根据一级分类、二级分类搜索课程信息
     *
     * @param pageParams        分页参数
     * @param courseSearchParam 课程条件
     * @return 课程列表
     */
    @Override
    public SearchPageResultDto<CourseIndex> queryCoursePublishList(PageParams pageParams,
                                                                   SearchCourseParamDto courseSearchParam) {
        // 1. 准备 request 对象
        SearchRequest searchRequest = new SearchRequest(courseIndexName);

        // 2. 组织 DSL 参数，这里使用布尔查询
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // source 源字段过滤
        String[] sourceFieldsArray = sourceFields.split(",");
        // 参数一:需要返回的字段; 参数二:不需要返回的字段
        searchSourceBuilder.fetchSource(sourceFieldsArray, new String[0]);

        // 3. 组织查询条件
        // 3.1 指定起始查询位置和查询条数
        Long pageNo = pageParams.getPageNo();
        Long pageSize = pageParams.getPageSize();
        int start = (int) ((pageNo - 1) * pageSize);
        searchSourceBuilder.from(start)
                .size(Math.toIntExact(pageSize));
        // 3.2 指定查询条件
        if (courseSearchParam == null) {
            courseSearchParam = new SearchCourseParamDto();
        }
        // 根据关键字搜索
        if (StringUtils.isNotEmpty(courseSearchParam.getKeywords())) {
            // 匹配关键字
            MultiMatchQueryBuilder multiMatchQueryBuilder =
                    QueryBuilders.multiMatchQuery(courseSearchParam.getKeywords(), "name", "description");
            // 设置匹配占比
            multiMatchQueryBuilder.minimumShouldMatch("70%");
            // 提升另个字段的 Boost 值
            multiMatchQueryBuilder.field("name", 10);
            boolQueryBuilder.must(multiMatchQueryBuilder);
        }
        // 根据分类、课程等级搜索采用过滤器实现
        if (StringUtils.isNotEmpty(courseSearchParam.getMt())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("mtName", courseSearchParam.getMt()));
        }
        if (StringUtils.isNotEmpty(courseSearchParam.getSt())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("stName", courseSearchParam.getSt()));
        }
        if (StringUtils.isNotEmpty(courseSearchParam.getGrade())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("grade", courseSearchParam.getGrade()));
        }

        // 4. 布尔查询
        searchSourceBuilder.query(boolQueryBuilder);
        // NOTE: 查询结果高亮设置
        searchSourceBuilder.highlighter(new HighlightBuilder()
                .field("name")
                .preTags("<font class='es-highlight'>")
                .postTags("</font>"));
        // 请求搜索
        searchRequest.source(searchSourceBuilder);
        // 聚合搜索
        buildAggregation(searchRequest);

        // 5. 发送请求，获取响应结果
        SearchResponse searchResponse;
        try {
            searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("课程搜索异常：{}", e.getMessage());
            return new SearchPageResultDto<>(new ArrayList<>(), 0, 0, 0);
        }

        // 6. 结果集处理
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        // 记录总数
        TotalHits totalHits = hits.getTotalHits();
        // 数据列表
        List<CourseIndex> list = new ArrayList<>();
        for (SearchHit hit : searchHits) {
            // 获取文档 source 并转为 CourseIndex 对象
            String sourceAsString = hit.getSourceAsString();
            CourseIndex courseIndex = JSON.parseObject(sourceAsString, CourseIndex.class);
            // 取出名称
            String name = courseIndex.getName();
            // 取出高亮字段内容
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            if (!CollectionUtils.isEmpty(highlightFields)) {
                HighlightField nameField = highlightFields.get("name");
                if (nameField != null) {
                    StringBuilder stringBuilder = new StringBuilder();
                    for (Text str : nameField.getFragments()) {
                        stringBuilder.append(str.string());
                    }
                    name = stringBuilder.toString();
                }
            }
            courseIndex.setName(name);
            list.add(courseIndex);
        }

        // 7. 封装查询结果
        SearchPageResultDto<CourseIndex> pageResult = new SearchPageResultDto<>(list, totalHits.value, pageNo, pageSize);
        List<String> mtList = getAggregation(searchResponse.getAggregations(), "mtAgg");
        List<String> stList = getAggregation(searchResponse.getAggregations(), "stAgg");
        pageResult.setMtList(mtList);
        pageResult.setStList(stList);
        return pageResult;
    }

    /**
     * 建立聚合
     *
     * @param request 请求
     */
    private void buildAggregation(SearchRequest request) {
        request.source().aggregation(AggregationBuilders
                .terms("mtAgg")
                .field("mtName")
                .size(100)
        );
        request.source().aggregation(AggregationBuilders
                .terms("stAgg")
                .field("stName")
                .size(100)
        );
    }

    /**
     * 根据聚合名称获取聚合结果
     *
     * @param aggregations 聚合对象
     * @param aggName      聚合名称
     * @return 聚合结果
     */
    private List<String> getAggregation(Aggregations aggregations, String aggName) {
        // 1.根据聚合名称获取聚合结果
        Terms brandTerms = aggregations.get(aggName);
        // 2.获取 buckets
        List<? extends Terms.Bucket> buckets = brandTerms.getBuckets();
        // 3.遍历
        List<String> brandList = new ArrayList<>();
        for (Terms.Bucket bucket : buckets) {
            // 4.获取 key
            String key = bucket.getKeyAsString();
            // 5.加入到集合中
            brandList.add(key);
        }
        return brandList;
    }

}
