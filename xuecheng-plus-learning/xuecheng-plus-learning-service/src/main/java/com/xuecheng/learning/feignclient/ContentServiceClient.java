package com.xuecheng.learning.feignclient;

import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.content.model.po.Teachplan;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 内容管理服务远程调用接口
 *
 * @author liujue
 */
@FeignClient(value = "content-api", fallbackFactory = ContentServiceClientFallbackFactory.class)
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
}
