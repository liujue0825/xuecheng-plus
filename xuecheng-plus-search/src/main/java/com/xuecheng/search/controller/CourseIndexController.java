package com.xuecheng.search.controller;

import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.search.po.CourseIndex;
import com.xuecheng.search.service.IndexService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 课程信息索引相关接口
 *
 * @author liujue
 */
@RestController
@RequestMapping("/index")
public class CourseIndexController {

    /**
     * 索引库
     */
    @Value("${elasticsearch.course.index}")
    private String courseIndexStore;

    private final IndexService indexService;

    @Autowired
    public CourseIndexController(IndexService indexService) {
        this.indexService = indexService;
    }

    /**
     * 添加课程索引接口
     */
    @PostMapping("/course")
    public Boolean add(@RequestBody CourseIndex courseIndex) {
        Long id = courseIndex.getId();
        if (id == null) {
            XueChengPlusException.cast("课程 id 为空");
        }
        Boolean result = indexService.addCourseIndex(courseIndexStore, String.valueOf(id), courseIndex);
        if (Boolean.FALSE.equals(result)) {
            XueChengPlusException.cast("添加课程索引失败");
        }
        return true;
    }
}
