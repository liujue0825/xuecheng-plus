package com.xuecheng.content.feignclient;

import com.xuecheng.content.feignclient.entity.CourseIndex;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 课程搜索服务远程调用接口
 *
 * @author liujue
 */
@FeignClient(value = "search",
        fallbackFactory = SearchServiceClient.SearchServiceClientFallbackFactory.class)
public interface SearchServiceClient {

    /**
     * 添加课程索引接口
     */
    @PostMapping("/search/index/course")
    Boolean add(@RequestBody CourseIndex courseIndex);

    /**
     * 服务降级处理，可以获取异常信息
     */
    @Slf4j
    class SearchServiceClientFallbackFactory implements FallbackFactory<SearchServiceClient> {
        @Override
        public SearchServiceClient create(Throwable cause) {
            log.error("远程调用课程索引添加服务时发生熔断，熔断异常是：{}", cause.getMessage());
            return null;
        }
    }
}
