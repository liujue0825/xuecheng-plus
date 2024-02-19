package com.xuecheng.content.feignclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 课程搜索服务远程调用接口
 *
 * @author liujue
 */
@FeignClient(value = "search")
public interface SearchServiceClient {

    /**
     * 添加课程索引接口
     */
    @PostMapping("/search/index/course")
    Boolean add(@RequestBody CourseIndex courseIndex);
}
