package com.xuecheng.content.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamDto;
import com.xuecheng.content.model.po.CourseBase;


/**
 * 课程信息相关业务接口
 *
 * @author liujue
 */
public interface CourseBaseInfoService {

    /**
     * 课程分页查询
     *
     * @param companyId         教学机构 id
     * @param pageParams        分页参数
     * @param queryCourseParams 查询条件
     * @return 分页查询结果
     */
    PageResult<CourseBase> queryCourseBaseList(Long companyId, PageParams pageParams, QueryCourseParamDto queryCourseParams);

    /**
     * 新增课程基本信息
     *
     * @param companyId    教学机构 id
     * @param addCourseDto 课程基本信息
     * @return 新增的课程基本信息
     */
    CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto addCourseDto);

    /**
     * 根据课程id查询课程基本信息
     *
     * @param courseId 课程 id
     * @return 课程基本信息
     */
    CourseBaseInfoDto getCourseBaseInfo(Long courseId);

    /**
     * 修改课程信息
     *
     * @param companyId 机构 id，本机构只能修改本机构课程
     * @return 修改后的课程基本信息
     */
    CourseBaseInfoDto updateCourseBase(Long companyId, EditCourseDto editCourseDto);

    /**
     * 根据课程 id 删除课程信息
     *
     * @param companyId 机构 id，本机构只能修改本机构课程
     * @param courseId  课程 id
     */
    void deleteCourse(Long companyId, Long courseId);
}
