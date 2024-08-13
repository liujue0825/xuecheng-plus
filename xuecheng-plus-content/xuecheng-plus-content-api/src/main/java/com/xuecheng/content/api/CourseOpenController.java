package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.service.CoursePublishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author liujue
 */
@RestController
@RequestMapping("/open")
public class CourseOpenController {

    private final CoursePublishService coursePublishService;

    @Autowired
    public CourseOpenController(CoursePublishService coursePublishService) {
        this.coursePublishService = coursePublishService;
    }

    /**
     * 获取课程计划接口
     */
    @GetMapping("/course/whole/{courseId}")
    public CoursePreviewDto getPreviewInfo(@PathVariable Long courseId) {
        return coursePublishService.getCoursePreviewInfo(courseId);
    }
}
