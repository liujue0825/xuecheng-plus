package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.po.CourseCategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author liujue
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseCategoryTreeDto extends CourseCategory {
    /**
     * 子节点列表
     */
    private List<CourseCategoryTreeDto> childrenTreeNodes;
}
