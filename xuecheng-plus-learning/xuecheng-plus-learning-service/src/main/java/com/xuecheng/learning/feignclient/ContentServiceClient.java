package com.xuecheng.learning.feignclient;

import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.content.model.po.Teachplan;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * 内容管理服务远程调用接口
 *
 * @author liujue
 */
@FeignClient(value = "content-api", fallbackFactory = ContentServiceClient.ContentServiceClientFallbackFactory.class)
public interface ContentServiceClient {

    /**
     * 获取课程发布信息
     */
    @GetMapping("/content/r/coursepublish/{courseId}")
    CoursePublish getCoursePublish(@PathVariable("courseId") Long courseId);

    /**
     * 获取教学计划信息
     */
    @PostMapping("/content/teachplan/{teachplanId}")
    Teachplan getTeachplan(@PathVariable Long teachplanId);

    @Slf4j
    class ContentServiceClientFallbackFactory implements FallbackFactory<ContentServiceClient> {
        @Override
        public ContentServiceClient create(Throwable throwable) {
            return new ContentServiceClient() {
                @Override
                public CoursePublish getCoursePublish(Long courseId) {
                    log.error("调用内容管理服务发生熔断:{}", throwable.toString(), throwable);
                    return null;
                }

                @Override
                public Teachplan getTeachplan(Long teachplanId) {
                    log.error("调用内容管理服务发生熔断:{}", throwable.toString(), throwable);
                    return null;
                }
            };
        }
    }
}
