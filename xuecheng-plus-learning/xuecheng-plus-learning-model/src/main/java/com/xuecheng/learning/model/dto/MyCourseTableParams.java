package com.xuecheng.learning.model.dto;

import lombok.Data;
import lombok.ToString;

/**
 * 我的课程查询条件类
 *
 * @author Mr.M
 */
@Data
@ToString
public class MyCourseTableParams {

    private String userId;

    /**
     * 课程类型  [{"code":"700001","desc":"免费课程"},{"code":"700002","desc":"收费课程"}]
     */
    private String courseType;

    /**
     * 排序 1按学习时间进行排序 2按加入时间进行排序
     */
    private String sortType;

    /**
     * 1 即将过期, 2已经过期
     */
    private String expiresType;

    private int page = 1;

    private int startIndex;

    private int size = 4;

}
