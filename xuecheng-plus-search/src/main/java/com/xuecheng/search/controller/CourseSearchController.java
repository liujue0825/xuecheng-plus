package com.xuecheng.search.controller;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.search.dto.SearchCourseParamDto;
import com.xuecheng.search.dto.SearchPageResultDto;
import com.xuecheng.search.po.CourseIndex;
import com.xuecheng.search.service.CourseSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 课程信息搜索相关接口
 *
 * @author liujue
 */
@RestController
@RequestMapping("/course")
public class CourseSearchController {

    private final CourseSearchService courseSearchService;

    @Autowired
    public CourseSearchController(CourseSearchService courseSearchService) {
        this.courseSearchService = courseSearchService;
    }

    /**
     * 课程信息搜索接口
     */
    @GetMapping("/list")
    public SearchPageResultDto<CourseIndex> list(PageParams pageParams, SearchCourseParamDto searchCourseParamDto) {
        return courseSearchService.queryCoursePublishList(pageParams, searchCourseParamDto);
    }
}
