package com.xuecheng.search.dto;

import com.xuecheng.base.model.PageResult;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 课程搜索分页结果响应类
 *
 * @author 刘珏
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SearchPageResultDto<T> extends PageResult<T> {

    /**
     * 大分类列表
     */
    private List<String> mtList;

    /**
     * 小分类列表
     */
    private List<String> stList;

    public SearchPageResultDto(List<T> items, long counts, long page, long pageSize) {
        super(items, counts, page, pageSize);
    }
}
