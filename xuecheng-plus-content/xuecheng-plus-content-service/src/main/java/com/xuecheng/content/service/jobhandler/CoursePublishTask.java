package com.xuecheng.content.service.jobhandler;

import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MessageProcessAbstract;
import com.xuecheng.messagesdk.service.MqMessageService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * 课程发布相关任务类
 *
 * @author liujue
 */
@Slf4j
@Component
public class CoursePublishTask extends MessageProcessAbstract {

    private final CoursePublishService coursePublishService;

    @Autowired
    public CoursePublishTask(CoursePublishService coursePublishService) {
        this.coursePublishService = coursePublishService;
    }

    @XxlJob("CoursePublishJobHandler")
    public void coursePublishJobHandler() {
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        process(shardIndex, shardTotal, "course_publish", 5, 60);
    }

    /**
     * 任务处理
     *
     * @param mqMessage 任务内容
     * @return 执行结果
     */
    @Override
    public boolean execute(MqMessage mqMessage) {
        log.debug("开始执行课程发布任务，课程id：{}", mqMessage.getBusinessKey1());
        String courseId = mqMessage.getBusinessKey1();
        // 1.第一阶段: 将课程信息静态页面上传至 MinIO
        saveCourseHtmlToMinio(mqMessage, Long.valueOf(courseId));
        // 2.第二阶段: 将课程索引信息存储到 ElasticSearch 中
        saveCourseIndexToEs(mqMessage, Long.valueOf(courseId));
        // TODO: 3.第三阶段: 存储到 Redis 中
        saveToRedis(mqMessage, Long.valueOf(courseId));
        return false;
    }

    /**
     * 将课程信息静态页面上传至 MinIO, 包括:
     * <p>
     * 1. 幂等性判断
     * 2. 生成并上传静态页面
     * 3. 更新当前阶段状态
     * </p>
     *
     * @param mqMessage 任务内容
     * @param courseId  课程 id
     */
    private void saveCourseHtmlToMinio(MqMessage mqMessage, Long courseId) {
        // 1. 幂等性判断:判断当前任务是否被处理过
        Long id = mqMessage.getId();
        MqMessageService mqMessageService = this.getMqMessageService();
        int stageOne = mqMessageService.getStageOne(id);
        if (stageOne == 1) {
            log.debug("当前阶段为静态化课程信息任务，已完成，无需再次处理，任务信息：{}", mqMessage);
            return;
        }
        // 2. 生成并上传静态页面
        File htmlFile = coursePublishService.generateCourseHtml(courseId);
        if (htmlFile == null) {
            XueChengPlusException.cast("课程静态化过程出现异常");
        }
        coursePublishService.uploadCourseHtml(courseId, htmlFile);
        // 3. 更新当前阶段状态
        mqMessageService.completedStageOne(id);
    }

    /**
     * 将课程索引信息存储到 ElasticSearch 中
     *
     * @param mqMessage 任务内容
     * @param courseId  课程 id
     */
    private void saveCourseIndexToEs(MqMessage mqMessage, Long courseId) {
        // 1. 幂等性判断:判断当前任务是否被处理过
        Long id = mqMessage.getId();
        MqMessageService mqMessageService = this.getMqMessageService();
        int stageTwo = mqMessageService.getStageTwo(id);
        if (stageTwo == 1) {
            log.debug("当前阶段为创建课程索引任务，已完成，无需再次处理，任务信息：{}", mqMessage);
            return;
        }
        // 2. 生成课程索引信息并存储
        Boolean result = coursePublishService.saveCourseIndex(courseId);
        // 3. 更新任务状态
        if (Boolean.TRUE.equals(result)) {
            mqMessageService.completedStageThree(id);
        }
    }

    private void saveToRedis(MqMessage mqMessage, Long courseId) {

    }
}
