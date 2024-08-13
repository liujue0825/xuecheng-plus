package com.xuecheng.search.dto;

import lombok.Data;
import lombok.ToString;

/**
 * 搜索课程请求参数类
 *
 * @author liujue
 */
@Data
@ToString
public class SearchCourseParamDto {

    /**
     * 关键字,搜索条件
     */
    private String keywords;

    /**
     * 大分类
     */
    private String mt;

    /**
     * 小分类
     */
    private String st;

    /**
     * 难度等级
     */
    private String grade;
}
