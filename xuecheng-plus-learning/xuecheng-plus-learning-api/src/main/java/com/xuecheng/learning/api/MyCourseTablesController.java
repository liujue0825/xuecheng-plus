package com.xuecheng.learning.api;

import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.learning.model.dto.MyCourseTableParams;
import com.xuecheng.learning.model.dto.XcChooseCourseDto;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.model.po.XcCourseTables;
import com.xuecheng.learning.service.MyCourseTablesService;
import com.xuecheng.learning.service.MyLearningService;
import com.xuecheng.learning.util.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 我的课程表接口
 *
 * @author Mr.M
 */
@Slf4j
@RestController
public class MyCourseTablesController {

    private final MyCourseTablesService myCourseTablesService;

    private final MyLearningService myLearningService;

    @Autowired
    public MyCourseTablesController(MyCourseTablesService myCourseTablesService,
                                    MyLearningService myLearningService) {
        this.myCourseTablesService = myCourseTablesService;
        this.myLearningService = myLearningService;
    }

    /**
     * 添加选课接口
     */
    @PostMapping("/choosecourse/{courseId}")
    public XcChooseCourseDto addChooseCourse(@PathVariable("courseId") Long courseId) {
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if (user == null) {
            XueChengPlusException.cast("请登陆后继续选课");
            return null;
        }
        String userId = user.getId();
        return myCourseTablesService.addChooseCourse(userId, courseId);
    }

    /**
     * 查询学习资格接口
     */
    @PostMapping("/choosecourse/learnstatus/{courseId}")
    public XcCourseTablesDto getLearnStatus(@PathVariable("courseId") Long courseId) {
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if (user == null) {
            XueChengPlusException.cast("请登陆后查询学习资格");
            return null;
        }
        String userId = user.getId();
        return myLearningService.getLearningStatus(userId, courseId);
    }

    /**
     * 分页查询我的课程表接口
     */
    @GetMapping("/mycoursetable")
    public PageResult<XcCourseTables> myCourseTable(MyCourseTableParams params) {
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if (user == null) {
            XueChengPlusException.cast("请登陆后继续查看课程");
            return null;
        }
        String userId = user.getId();
        params.setUserId(userId);
        return myCourseTablesService.queryMyCourseTables(params);
    }
}
