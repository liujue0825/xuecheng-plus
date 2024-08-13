package com.xuecheng.learning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.constants.CourseFeeStatus;
import com.xuecheng.base.constants.CourseLearningEligibility;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.base.utils.StringUtil;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.learning.feignclient.ContentServiceClient;
import com.xuecheng.learning.feignclient.MediaServiceClient;
import com.xuecheng.learning.mapper.XcCourseTablesMapper;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.model.po.XcCourseTables;
import com.xuecheng.learning.service.MyLearningService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * @author liujue
 */
@Slf4j
@Service
public class MyLearningServiceImpl implements MyLearningService {

    private final XcCourseTablesMapper xcCourseTablesMapper;

    private final ContentServiceClient contentServiceClient;

    private final MediaServiceClient mediaServiceClient;

    @Autowired
    public MyLearningServiceImpl(XcCourseTablesMapper xcCourseTablesMapper,
                                 ContentServiceClient contentServiceClient,
                                 MediaServiceClient mediaServiceClient) {
        this.xcCourseTablesMapper = xcCourseTablesMapper;
        this.contentServiceClient = contentServiceClient;
        this.mediaServiceClient = mediaServiceClient;
    }

    /**
     * 获取学习资格
     * 1. 查询我的课程表，如果查不到，则说明没有选课，返回状态码为 "702002" 的对象
     * 2. 如果查到了选课，判断是否过期
     * 2.1 如果过期则不能学习，返回状态码为 "702003" 的对象
     * 2.2 未过期可以学习，返回状态码为 "702001" 的对象
     *
     * @param userId   用户 id
     * @param courseId 课程 id
     * @return 学习资格实体类
     */
    @Override
    public XcCourseTablesDto getLearningStatus(String userId, Long courseId) {
        XcCourseTablesDto xcCourseTablesDto = new XcCourseTablesDto();
        // 1. 查询我的课程表，如果查不到，则说明没有选课，返回状态码为"702002" 的对象
        LambdaQueryWrapper<XcCourseTables> queryWrapper = new LambdaQueryWrapper<XcCourseTables>()
                .eq(XcCourseTables::getCourseId, courseId)
                .eq(XcCourseTables::getUserId, userId);
        XcCourseTables xcCourseTables = xcCourseTablesMapper.selectOne(queryWrapper);
        if (xcCourseTables == null) {
            // 没有选课或选课后没有支付
            xcCourseTablesDto.setLearnStatus(CourseLearningEligibility.NOT_SELECTED_OR_UNPAID.getCode());
            return xcCourseTablesDto;
        }
        BeanUtils.copyProperties(xcCourseTables, xcCourseTablesDto);
        // 2. 如果查到了选课，判断是否过期
        boolean isExpire = LocalDateTime.now().isAfter(xcCourseTables.getValidtimeEnd());
        // 2.1 如果过期则不能学习，返回状态码为"702003" 的对象
        if (isExpire) {
            // 已过期需要申请续期或重新支付
            xcCourseTablesDto.setLearnStatus(CourseLearningEligibility.EXPIRED_RENEWAL_REQUIRED.getCode());
        } else {
            // 2.2 未过期可以学习，返回状态码为 "702001" 的对象
            xcCourseTablesDto.setLearnStatus(CourseLearningEligibility.ELIGIBLE.getCode());
        }
        return xcCourseTablesDto;
    }

    /**
     * 获取视频 url
     *
     * @param userId      用户 id
     * @param courseId    课程 id
     * @param teachplanId 教学计划 id
     * @param mediaId     媒资信息 id
     * @return 视频 url
     */
    @Override
    public RestResponse<String> getVideo(String userId, Long courseId, Long teachplanId, String mediaId) {
        // 1. 远程调用内容管理服务，查询课程信息
        CoursePublish coursePublish = contentServiceClient.getCoursePublish(courseId);
        if (coursePublish == null) {
            return RestResponse.fail("课程信息不存在");
        }
        // 2. 判断试学规则，远程调用内容管理服务，查询教学计划 teachplan
        Teachplan teachplan = contentServiceClient.getTeachplan(teachplanId);
        // 2.1 isPreview 字段为1表示支持试学，返回课程url
        if ("1".equals(teachplan.getIsPreview())) {
            return mediaServiceClient.getPlayUrlByMediaId(mediaId);
        }
        // 3. 非试学,登录状态
        if (StringUtil.isNotEmpty(userId)) {
            // 3.1 判断是否选课
            XcCourseTablesDto learningStatus = getLearningStatus(userId, courseId);
            String learnStatus = learningStatus.getLearnStatus();
            if (CourseLearningEligibility.NOT_SELECTED_OR_UNPAID.getCode().equals(learnStatus)) {
                RestResponse.fail("没有选课或选课后没有支付");
            } else if (CourseLearningEligibility.EXPIRED_RENEWAL_REQUIRED.getCode().equals(learnStatus)) {
                RestResponse.fail("已过期需要申请续期或重新支付");
            } else {
                return mediaServiceClient.getPlayUrlByMediaId(mediaId);
            }
        }
        // 4. 非试学，未登录状态
        String charge = coursePublish.getCharge();
        // 4.1 免费课程，返回课程url
        if (CourseFeeStatus.FREE.getCode().equals(charge)) {
            return mediaServiceClient.getPlayUrlByMediaId(mediaId);
        }
        return RestResponse.fail("请购买课程后学习");
    }
}
