package com.xuecheng.search.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ElasticSearch 相关配置类
 *
 * <p>
 * 注: 如果配置 ElasticSearch集群, 则需要对 hostlist 进行处理
 *     {@code @Bean}
 *     public RestHighLevelClient restHighLevelClient() {
 *         // 解析 hostlist 配置信息
 *         String[] split = hostlist.split(",");
 *         // 创建HttpHost数组，其中存放es主机和端口的配置信息
 *         HttpHost[] httpHostArray = new HttpHost[split.length];
 *         for (int i = 0; i < split.length; i++) {
 *             String item = split[i];
 *             httpHostArray[i] = new HttpHost(item.split(":")[0], Integer.parseInt(item.split(":")[1]), "http");
 *         }
 *         // 创建RestHighLevelClient客户端
 *         return new RestHighLevelClient(RestClient.builder(httpHostArray));
 *     }
 * </p>
 *
 * @author liujue
 */
@Configuration
public class ElasticSearchConfig {

    /**
     * es 地址, list 的原因是可能是 es 集群
     */
    @Value("${elasticsearch.hostlist}")
    private String hostlist;

    @Bean
    public RestHighLevelClient restHighLevelClient() {
        return new RestHighLevelClient(RestClient.builder(hostlist));
    }

}