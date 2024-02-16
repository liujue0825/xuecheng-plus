package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;

import java.util.List;

/**
 * @author liujue
 */
public interface TeachplanService {
    List<TeachplanDto> findTeachplanTree(Long courseId);

    void saveTeachplan(Teachplan teachplan);

    void deleteTeachplan(Long teachplanId);

    void orderByTeachplan(String moveType, Long teachplanId);

    /**
     * 绑定媒资计划
     *
     * @param bindTeachplanMediaDto 请求类
     */
    void associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto);

    /**
     * 解绑教学计划与媒资信息
     *
     * @param teachplanId 教学计划id
     * @param mediaId     媒资信息id
     */
    void unassociationMedia(Long teachplanId, Long mediaId);
}
