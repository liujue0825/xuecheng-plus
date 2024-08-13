package com.xuecheng.media.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO 相关配置类
 *
 * @author liujue
 */
@Configuration
public class MinIOConfig {

    /**
     * 连接地址
     */
    @Value("${minio.endpoint}")
    private String endpoint;

    /**
     * 账号
     */
    @Value("${minio.accessKey}")
    private String accessKey;

    /**
     * 密码
     */
    @Value("${minio.secretKey}")
    private String secretKey;

    /**
     * 构造 MinIO 客户端
     *
     * @return MinIO 客户端对象
     */
    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }
}
