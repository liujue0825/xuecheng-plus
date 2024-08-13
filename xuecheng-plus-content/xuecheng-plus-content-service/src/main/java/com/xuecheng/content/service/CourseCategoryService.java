package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CourseCategoryTreeDto;

import java.util.List;

/**
 * @author liujue
 */
public interface CourseCategoryService {

    /**
     * 课程分类查询
     *
     * @param id 根节点 id
     * @return 根节点下面的所有子节点
     */
    List<CourseCategoryTreeDto> queryTreeNodes(String id);
}
