package com.xuecheng.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 课程计划 Mapper 接口
 * </p>
 *
 * @author liujue
 */
public interface TeachplanMapper extends BaseMapper<Teachplan> {

    /**
     * 查找树形课程计划表
     *
     * @param courseId 课程 id
     * @return 课程列表
     */
    List<TeachplanDto> selectTreeNodes(Long courseId);

    /**
     * 找到上一个章节的 orderby
     *
     * @param courseId 课程 id
     * @param grade    章节标识
     * @param orderby  排序字段
     */
    Teachplan selectPrevChapter(Long courseId, int grade, int orderby);

    /**
     * 找到下一个章节的 orderby
     *
     * @param courseId 课程 id
     * @param grade    章节标识
     * @param orderby  排序字段
     */
    Teachplan selectNextChapter(Long courseId, int grade, int orderby);

    /**
     * 找到上一个章节的 orderby
     *
     * @param parentId 章节 id
     * @param orderby  排序字段
     */
    Teachplan selectPrevSection(Long parentId, int orderby);

    /**
     * 找到下一个章节的 orderby
     *
     * @param parentId 章节 id
     * @param orderby  排序字段
     */
    Teachplan selectNextSection(Long parentId, int orderby);
}
