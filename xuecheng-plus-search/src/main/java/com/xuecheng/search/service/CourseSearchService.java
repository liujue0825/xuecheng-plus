package com.xuecheng.search.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.search.dto.SearchCourseParamDto;
import com.xuecheng.search.dto.SearchPageResultDto;
import com.xuecheng.search.po.CourseIndex;

/**
 * 课程搜索相关业务
 *
 * @author liujue
 */
public interface CourseSearchService {

    /**
     * 搜索课程列表
     *
     * @param pageParams           分页参数
     * @param searchCourseParamDto 课程条件
     * @return 课程列表
     */
    SearchPageResultDto<CourseIndex> queryCoursePublishList(PageParams pageParams, SearchCourseParamDto searchCourseParamDto);
}
