package com.xuecheng.learning.service;

import com.xuecheng.base.model.PageResult;
import com.xuecheng.learning.model.dto.MyCourseTableParams;
import com.xuecheng.learning.model.dto.XcChooseCourseDto;
import com.xuecheng.learning.model.po.XcCourseTables;

/**
 * 我的课程表相关业务
 *
 * @author liujue
 */
public interface MyCourseTablesService {

    /**
     * 添加选课
     *
     * @param userId   用户id
     * @param courseId 课程id
     * @return 选课信息
     */
    XcChooseCourseDto addChooseCourse(String userId, Long courseId);

    /**
     * 保存选课记录
     *
     * @param chooseCourseId 选课 id
     * @return 成功/失败
     */
    boolean saveChooseCourseStatus(String chooseCourseId);

    /**
     * 分页查询课程表
     *
     * @param params 查询参数类
     * @return 课程表
     */
    PageResult<XcCourseTables> queryMyCourseTables(MyCourseTableParams params);
}
