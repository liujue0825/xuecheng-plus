package com.xuecheng.learning.service;

import com.xuecheng.base.model.RestResponse;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;

/**
 * 我的学习相关业务
 *
 * @author liujue
 */
public interface MyLearningService {

    /**
     * 获取学习资格
     *
     * @param userId   用户 id
     * @param courseId 课程 id
     * @return 学习资格实体类
     */
    XcCourseTablesDto getLearningStatus(String userId, Long courseId);

    /**
     * 获取视频 url
     *
     * @param userId      用户 id
     * @param courseId    课程 id
     * @param teachplanId 教学计划 id
     * @param mediaId     媒资信息 id
     * @return 视频 url
     */
    RestResponse<String> getVideo(String userId,  Long courseId, Long teachplanId, String mediaId);
}
