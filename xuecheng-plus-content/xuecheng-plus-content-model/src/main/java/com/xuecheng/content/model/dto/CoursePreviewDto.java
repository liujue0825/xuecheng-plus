package com.xuecheng.content.model.dto;

import lombok.Data;

import java.util.List;

/**
 * 课程预览相关信息类
 * @author liujue
 */
@Data
public class CoursePreviewDto {

    /**
     * 课程基本信息和课程营销信息
     */
    private CourseBaseInfoDto courseBaseInfoDto;

    /**
     * 课程计划信息
     */
    private List<TeachplanDto> teachplanList;

    // TODO: 课程师资信息
}
