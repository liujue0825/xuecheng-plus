package com.xuecheng.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.search.service.IndexService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @author liujue
 */
@Slf4j
@Service
public class IndexServiceImpl implements IndexService {

    private final RestHighLevelClient client;

    @Autowired
    public IndexServiceImpl(RestHighLevelClient client) {
        this.client = client;
    }

    /**
     * 添加课程索引
     *
     * @param indexName 索引名称
     * @param id        id
     * @param object    索引对象
     * @return 成功/失败
     */
    @Override
    public Boolean addCourseIndex(String indexName, String id, Object object) {
        // 1. 创建 request 对象
        IndexRequest indexRequest = new IndexRequest(indexName).id(id);

        // 2. 准备请求参数
        String jsonString = JSON.toJSONString(object);
        // 指定索引文档内容
        indexRequest.source(jsonString, XContentType.JSON);

        // 索引响应对象
        IndexResponse indexResponse = null;
        try {
            // 3. 发送请求
            indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("添加索引出错: {}", e.getMessage());
            XueChengPlusException.cast("添加索引出错");
        }
        String name = indexResponse.getResult().name();
        // 若文档不存在，则为 created，若文档存在，则为 updated，若两者均不是，就是出错了
        return "CREATED".equalsIgnoreCase(name) || "UPDATED".equalsIgnoreCase(name);    // 注意使用忽略大小写的 API
    }

    /**
     * 更新课程索引
     *
     * @param indexName 索引名称
     * @param id        id
     * @param object    索引对象
     * @return 成功/失败
     */
    @Override
    public Boolean updateCourseIndex(String indexName, String id, Object object) {
        UpdateRequest updateRequest = new UpdateRequest(indexName, id);

        String jsonString = JSON.toJSONString(object);
        updateRequest.doc(jsonString, XContentType.JSON);

        UpdateResponse updateResponse = null;
        try {
            updateResponse = client.update(updateRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("更新索引出错: {}", e.getMessage());
            XueChengPlusException.cast("更新索引出错");
        }
        DocWriteResponse.Result result = updateResponse.getResult();
        return "UPDATED".equalsIgnoreCase(result.name());

    }

    /**
     * 删除课程索引
     *
     * @param indexName 索引名称
     * @param id        主键
     * @return 成功/失败
     */
    @Override
    public Boolean deleteCourseIndex(String indexName, String id) {
        // 删除索引请求对象
        DeleteRequest deleteRequest = new DeleteRequest(indexName, id);

        // 响应对象
        DeleteResponse deleteResponse = null;
        try {
            deleteResponse = client.delete(deleteRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("删除索引出错: {}", e.getMessage());
            XueChengPlusException.cast("删除索引出错");
        }
        // 获取响应结果
        DocWriteResponse.Result result = deleteResponse.getResult();
        return "DELETED".equalsIgnoreCase(result.name());
    }
}
