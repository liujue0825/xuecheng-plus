package com.xuecheng.learning.feignclient;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author liujue
 */
@Slf4j
@Component
public class ContentServiceClientFallbackFactory implements FallbackFactory<ContentServiceClient> {
    @Override
    public ContentServiceClient create(Throwable throwable) {
        return courseId -> {
            log.error("调用内容管理服务发生熔断:{}", throwable.toString(),throwable);
            return null;
        };
    }
}
