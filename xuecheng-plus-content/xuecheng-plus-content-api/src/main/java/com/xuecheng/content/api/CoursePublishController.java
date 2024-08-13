package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.content.service.CoursePublishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;


/**
 * 课程发布相关接口
 *
 * @author liujue
 */
@RestController
public class CoursePublishController {

    private final CoursePublishService coursePublishService;

    @Autowired
    public CoursePublishController(CoursePublishService coursePublishService) {
        this.coursePublishService = coursePublishService;
    }

    /**
     * 课程预览接口
     */
    @GetMapping("/coursepreview/{courseId}")
    public ModelAndView preview(@PathVariable Long courseId) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("course_template");
        CoursePreviewDto coursePreviewInfo = coursePublishService.getCoursePreviewInfo(courseId);
        modelAndView.addObject("model", coursePreviewInfo);
        return modelAndView;
    }

    /**
     * 提交审核接口
     */
    @PostMapping("/courseaudit/commit/{courseId}")
    public void commitAudit(@PathVariable Long courseId) {
        Long companyId = 1232141425L;
        coursePublishService.commitAudit(companyId, courseId);
    }

    /**
     * 课程发布接口
     */
    @GetMapping("/coursepulish/{courseId}")
    public void coursePublish(@PathVariable Long courseId) {
        Long companyId = 1232141425L;
        coursePublishService.publishCourse(companyId, courseId);
    }

    /**
     * 查询课程发布信息
     */
    @GetMapping("/r/coursepublish/{courseId}")
    public CoursePublish getCoursePublish(@PathVariable("courseId") Long courseId) {
        return coursePublishService.getCoursePublish(courseId);
    }
}
