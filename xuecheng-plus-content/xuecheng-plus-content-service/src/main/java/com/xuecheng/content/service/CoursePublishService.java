package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.po.CoursePublish;

import java.io.File;

/**
 * 课程发布相关业务
 *
 * @author liujue
 */
public interface CoursePublishService {

    /**
     * 根据课程 id 得到课程预览相关信息
     *
     * @param courseId 课程 id
     * @return 课程预览相关信息
     */
    CoursePreviewDto getCoursePreviewInfo(Long courseId);

    /**
     * 提交审核
     *
     * @param companyId 机构 id
     * @param courseId  课程 id
     */
    void commitAudit(Long companyId, Long courseId);

    /**
     * 课程发布
     *
     * @param companyId 机构 id
     * @param courseId  课程 id
     */
    void publishCourse(Long companyId, Long courseId);

    /**
     * 课程页面静态化, 生成 html 页面
     *
     * @param courseId 课程 id
     * @return html 文件
     */
    File generateCourseHtml(Long courseId);

    /**
     * 上传课程静态化页面
     *
     * @param courseId 课程 id
     * @param file     html 页面
     */
    void uploadCourseHtml(Long courseId, File file);

    /**
     * 保存课程索引信息
     *
     * @param courseId 课程 id
     * @return 成功/失败
     */
    Boolean saveCourseIndex(Long courseId);

    /**
     * 查询课程发布信息
     *
     * @param courseId 课程 id
     * @return 课程发布信息
     */
    CoursePublish getCoursePublish(Long courseId);
}
