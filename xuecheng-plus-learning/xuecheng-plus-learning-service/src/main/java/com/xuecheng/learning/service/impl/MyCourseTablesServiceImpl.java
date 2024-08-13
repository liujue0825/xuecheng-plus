package com.xuecheng.learning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.constants.CourseFeeStatus;
import com.xuecheng.base.constants.CourseSelectionStatus;
import com.xuecheng.base.constants.CourseSelectionType;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.learning.feignclient.ContentServiceClient;
import com.xuecheng.learning.mapper.XcChooseCourseMapper;
import com.xuecheng.learning.mapper.XcCourseTablesMapper;
import com.xuecheng.learning.model.dto.MyCourseTableParams;
import com.xuecheng.learning.model.dto.XcChooseCourseDto;
import com.xuecheng.learning.model.po.XcChooseCourse;
import com.xuecheng.learning.model.po.XcCourseTables;
import com.xuecheng.learning.service.MyCourseTablesService;
import com.xuecheng.learning.service.MyLearningService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author liujue
 */
@Slf4j
@Service
public class MyCourseTablesServiceImpl implements MyCourseTablesService {

    private final XcChooseCourseMapper xcChooseCourseMapper;

    private final XcCourseTablesMapper xcCourseTablesMapper;

    private final ContentServiceClient contentServiceClient;

    private final MyLearningService myLearningService;

    @Autowired
    public MyCourseTablesServiceImpl(XcChooseCourseMapper xcChooseCourseMapper,
                                     XcCourseTablesMapper xcCourseTablesMapper,
                                     ContentServiceClient contentServiceClient,
                                     MyLearningService myLearningService) {
        this.xcChooseCourseMapper = xcChooseCourseMapper;
        this.xcCourseTablesMapper = xcCourseTablesMapper;
        this.contentServiceClient = contentServiceClient;
        this.myLearningService = myLearningService;
    }

    /**
     * 添加选课
     * 1. 判断收费标准
     * 2. 根据课程类型进行不同处理
     * 2.1 免费课程：添加选课记录表及我的课程表
     * 2.2 收费课程：添加选课记录表
     * 3. 判断学生的学习资格
     *
     * @param userId   用户id
     * @param courseId 课程id
     * @return 选课信息
     */
    @Override
    @Transactional
    public XcChooseCourseDto addChooseCourse(String userId, Long courseId) {
        // 1. 判断收费标准
        CoursePublish coursePublish = contentServiceClient.getCoursePublish(courseId);
        String charge = coursePublish.getCharge();
        XcChooseCourse xcChooseCourse = new XcChooseCourse();
        xcChooseCourse.setCourseId(coursePublish.getId());
        xcChooseCourse.setCourseName(coursePublish.getName());
        xcChooseCourse.setUserId(userId);
        xcChooseCourse.setCompanyId(coursePublish.getCompanyId());
        // 2. 根据课程类型进行不同处理
        if (CourseFeeStatus.FREE.getCode().equals(charge)) {
            // 2.1 免费课程：添加选课记录表及我的课程表
            xcChooseCourse = addFreeCourse(userId, coursePublish, xcChooseCourse);
            addCourseTables(xcChooseCourse);
        } else {
            // 2.2 收费课程：添加选课记录表
            xcChooseCourse = addChargeCourse(userId, coursePublish, xcChooseCourse);
        }
        // 3. 判断学生的学习资格
        String learnStatus = myLearningService.getLearningStatus(userId, courseId).getLearnStatus();
        // 4. 返回选课信息
        XcChooseCourseDto result = new XcChooseCourseDto();
        BeanUtils.copyProperties(xcChooseCourse, result);
        result.setLearnStatus(learnStatus);
        return result;
    }

    /**
     * 保存选课记录
     *
     * @param chooseCourseId 选课 id
     * @return 成功/失败
     */
    @Override
    @Transactional
    public boolean saveChooseCourseStatus(String chooseCourseId) {
        XcChooseCourse xcChooseCourse = xcChooseCourseMapper.selectById(chooseCourseId);
        if (xcChooseCourse == null) {
            log.error("接收到购买课程的消息，根据选课id未查询到课程，选课id：{}", chooseCourseId);
            return false;
        }
        if (CourseSelectionStatus.PENDING_PAYMENT.getCode().equals(xcChooseCourse.getStatus())) {
            xcChooseCourse.setStatus(CourseSelectionStatus.SUCCESS.getCode());
            int row = xcChooseCourseMapper.updateById(xcChooseCourse);
            if (row <= 0) {
                log.error("更新选课记录失败：{}", xcChooseCourse);
            }
        }
        addCourseTables(xcChooseCourse);
        return true;
    }

    /**
     * 分页查询课程表
     *
     * @param params 查询参数类
     * @return 课程表
     */
    @Override
    public PageResult<XcCourseTables> queryMyCourseTables(MyCourseTableParams params) {
        int pageNo = params.getPage();
        int pageSize = params.getSize();
        LambdaQueryWrapper<XcCourseTables> queryWrapper = new LambdaQueryWrapper<XcCourseTables>()
                .eq(XcCourseTables::getUserId, params.getUserId());
        Page<XcCourseTables> page = xcCourseTablesMapper.selectPage(new Page<>(pageNo, pageSize), queryWrapper);
        long counts = page.getSize();
        List<XcCourseTables> result = page.getRecords();
        return new PageResult<>(result, counts, pageNo, pageSize);
    }

    /**
     * 将免费课程添加到选课信息表中
     *
     * @param userId        用户 id
     * @param coursePublish 课程信息
     * @return 选课记录
     */
    private XcChooseCourse addFreeCourse(String userId, CoursePublish coursePublish, XcChooseCourse xcChooseCourse) {
        // 1. 数据库保存了全部类型的选课信息，需要先过滤相同数据的情况
        LambdaQueryWrapper<XcChooseCourse> queryWrapper = new LambdaQueryWrapper<XcChooseCourse>()
                .eq(XcChooseCourse::getUserId, userId)
                .eq(XcChooseCourse::getCourseId, coursePublish.getId())
                // 免费课程
                .eq(XcChooseCourse::getOrderType, CourseSelectionType.FREE.getCode())
                // 选课成功
                .eq(XcChooseCourse::getStatus, "701007");
        List<XcChooseCourse> xcChooseCourseList = xcChooseCourseMapper.selectList(queryWrapper);
        if (!CollectionUtils.isEmpty(xcChooseCourseList)) {
            return xcChooseCourseList.get(0);
        }
        // 免费课程
        xcChooseCourse.setOrderType(CourseSelectionType.FREE.getCode());
        xcChooseCourse.setCreateDate(LocalDateTime.now());
        xcChooseCourse.setCoursePrice(coursePublish.getPrice());
        // 免费课程有效期默认为一年
        xcChooseCourse.setValidDays(365);
        // 选课成功
        xcChooseCourse.setStatus("701001");
        xcChooseCourse.setValidtimeStart(LocalDateTime.now());
        xcChooseCourse.setValidtimeEnd(LocalDateTime.now().plusDays(365));
        int row = xcChooseCourseMapper.insert(xcChooseCourse);
        if (row <= 0) {
            XueChengPlusException.cast("添加选课记录失败");
        }
        return xcChooseCourse;
    }

    /**
     * 将收费课程添加到选课信息表中
     *
     * @param userId        用户 id
     * @param coursePublish 课程信息
     * @return 选课记录
     */
    private XcChooseCourse addChargeCourse(String userId, CoursePublish coursePublish, XcChooseCourse xcChooseCourse) {
        // 1. 数据库保存了全部类型的选课信息，需要先过滤相同数据的情况
        LambdaQueryWrapper<XcChooseCourse> queryWrapper = new LambdaQueryWrapper<XcChooseCourse>()
                .eq(XcChooseCourse::getUserId, userId)
                .eq(XcChooseCourse::getCourseId, coursePublish.getId())
                // 收费课程
                .eq(XcChooseCourse::getOrderType, CourseSelectionType.PAID.getCode())
                // 待支付
                .eq(XcChooseCourse::getStatus, "701002");
        List<XcChooseCourse> xcChooseCourseList = xcChooseCourseMapper.selectList(queryWrapper);
        if (!CollectionUtils.isEmpty(xcChooseCourseList)) {
            return xcChooseCourseList.get(0);
        }
        // 收费课程
        xcChooseCourse.setOrderType("700002");
        xcChooseCourse.setCreateDate(LocalDateTime.now());
        xcChooseCourse.setCoursePrice(coursePublish.getPrice());
        xcChooseCourse.setValidDays(365);   // 暂时写死为 365 天
        // 待支付
        xcChooseCourse.setStatus("701002");
        xcChooseCourse.setValidtimeStart(LocalDateTime.now());
        xcChooseCourse.setValidtimeEnd(LocalDateTime.now().plusDays(365));
        int row = xcChooseCourseMapper.insert(xcChooseCourse);
        if (row <= 0) {
            XueChengPlusException.cast("添加选课记录失败");
        }
        return xcChooseCourse;
    }

    /**
     * 将选课记录添加到我的课程表中
     * 1. 选课成功才可以添加
     * 2. 如果我的课程表已经存在课程，课程可能已经过期，如果有新的选课记录，则需要更新我的课程表中的现有信息
     *
     * @param xcChooseCourse 选课记录
     */
    private void addCourseTables(XcChooseCourse xcChooseCourse) {
        String status = xcChooseCourse.getStatus();
        if (!"701001".equals(status)) {
            XueChengPlusException.cast("选课未成功，无法添加到课程表");
        }
        LambdaQueryWrapper<XcCourseTables> queryWrapper = new LambdaQueryWrapper<XcCourseTables>()
                .eq(XcCourseTables::getCourseId, xcChooseCourse.getCourseId())
                .eq(XcCourseTables::getUserId, xcChooseCourse.getUserId());
        XcCourseTables xcCourseTables = xcCourseTablesMapper.selectOne(queryWrapper);
        if (xcCourseTables != null) {
            return;
        }
        xcCourseTables = new XcCourseTables();
        BeanUtils.copyProperties(xcChooseCourse, xcCourseTables);
        xcCourseTables.setChooseCourseId(xcChooseCourse.getId());
        xcCourseTables.setCourseType(xcChooseCourse.getOrderType());
        xcCourseTables.setUpdateDate(LocalDateTime.now());
        int row = xcCourseTablesMapper.insert(xcCourseTables);
        if (row <= 0) {
            XueChengPlusException.cast("添加到我的课程表中失败");
        }
    }
}
