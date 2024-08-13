package com.xuecheng.content.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.base.constants.CourseAuditStatus;
import com.xuecheng.base.constants.CoursePublishStatus;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.config.MultipartSupportConfig;
import com.xuecheng.content.feignclient.MediaServiceClient;
import com.xuecheng.content.feignclient.SearchServiceClient;
import com.xuecheng.content.feignclient.entity.CourseIndex;
import com.xuecheng.content.mapper.*;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.*;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.content.service.TeachplanService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MqMessageService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * @author liujue
 */
@Slf4j
@Service
public class CoursePublishServiceImpl implements CoursePublishService {

    private final CourseBaseInfoService courseBaseInfoService;

    private final TeachplanService teachplanService;

    private final CourseBaseMapper courseBaseMapper;

    private final CourseMarketMapper courseMarketMapper;

    private final CoursePublishPreMapper coursePublishPreMapper;

    private final CoursePublishMapper coursePublishMapper;

    private final MqMessageService mqMessageService;

    private final MediaServiceClient mediaServiceClient;

    private final SearchServiceClient searchServiceClient;

    @Autowired
    public CoursePublishServiceImpl(CourseBaseInfoService courseBaseInfoService,
                                    TeachplanService teachplanService,
                                    CourseBaseMapper courseBaseMapper,
                                    CourseMarketMapper courseMarketMapper,
                                    CoursePublishPreMapper coursePublishPreMapper,
                                    CoursePublishMapper coursePublishMapper,
                                    MqMessageService mqMessageService,
                                    MediaServiceClient mediaServiceClient,
                                    SearchServiceClient searchServiceClient) {
        this.courseBaseInfoService = courseBaseInfoService;
        this.teachplanService = teachplanService;
        this.courseBaseMapper = courseBaseMapper;
        this.courseMarketMapper = courseMarketMapper;
        this.coursePublishPreMapper = coursePublishPreMapper;
        this.coursePublishMapper = coursePublishMapper;
        this.mqMessageService = mqMessageService;
        this.mediaServiceClient = mediaServiceClient;
        this.searchServiceClient = searchServiceClient;
    }

    /**
     * 根据课程 id 得到课程预览相关信息
     * 包括:课程基本信息、课程营销信息、课程计划信息、课程师资信息
     *
     * @param courseId 课程 id
     * @return 课程预览相关信息
     */
    @Override
    public CoursePreviewDto getCoursePreviewInfo(Long courseId) {
        CoursePreviewDto result = new CoursePreviewDto();
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);
        result.setCourseBaseInfoDto(courseBaseInfo);
        List<TeachplanDto> teachplanTree = teachplanService.findTeachplanTree(courseId);
        result.setTeachplanList(teachplanTree);
        return result;
    }

    /**
     * 提交审核
     * 1. 根据传入的 courseId，查询课程基本信息、课程营销信息、课程计划信息，并汇总成课程预发布信息
     * 2. 向课程预发布表中插入我们汇总好的信息，如果已经存在，则更新，并状态为已提交
     * 3. 更新课程基本信息表的审核状态为已提交
     *
     * @param courseId 课程 id
     */
    @Transactional
    @Override
    public void commitAudit(Long companyId, Long courseId) {
        List<TeachplanDto> teachplanTree = teachplanService.findTeachplanTree(courseId);
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);

        // 约束检查
        // 1. 当前审核状态为已提交不允许再次提交
        if (CourseAuditStatus.SUBMITTED.getCode().equals(courseBaseInfo.getAuditStatus())) {
            XueChengPlusException.cast("当前审核状态为已提交不允许再次提交");
        }
        // 2. 本机构只允许提交本机构的课程
        if (!companyId.equals(courseBaseInfo.getCompanyId())) {
            XueChengPlusException.cast("本机构只允许提交本机构的课程");
        }
        // 3. 没有上传图片，不允许提交
        if (StringUtils.isEmpty(courseBaseInfo.getPic())) {
            XueChengPlusException.cast("没有上传课程封面，不允许提交审核");
        }
        // 4. 没有添加课程计划，不允许提交审核
        if (teachplanTree.isEmpty()) {
            XueChengPlusException.cast("没有添加课程计划，不允许提交审核");
        }

        // 构造课程预发布相关实体类
        CoursePublishPre result = new CoursePublishPre();
        BeanUtils.copyProperties(courseBaseInfo, result);
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        result.setMarket(JSON.toJSONString(courseMarket));
        result.setTeachplan(JSON.toJSONString(teachplanTree));
        // TODO: 教师信息未做
        result.setCompanyId(companyId);
        result.setCreateDate(LocalDateTime.now());
        // 首次提交, 设置发布状态为已提交
        result.setStatus(CourseAuditStatus.SUBMITTED.getCode());

        // 向课程预发布表中插入信息
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if (coursePublishPre != null) {
            coursePublishPreMapper.updateById(result);
        } else {
            coursePublishPreMapper.insert(result);
        }

        // 更新课程基本信息表的审核状态为已提交
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        courseBase.setAuditStatus(CourseAuditStatus.SUBMITTED.getCode());
        courseBaseMapper.updateById(courseBase);
    }

    /**
     * 课程发布
     * 1. 向 course_publish 表中插入一条记录，如果存在则更新，发布状态为已发布
     * 2. 更新 course_base 表的课程发布状态为已发布
     * 3. 向 mq_message 表插入一条消息，消息类型为 course_publish
     * 4. 删除课程预发布表的对应记录
     *
     * @param companyId 机构 id
     * @param courseId  课程 id
     */
    @Override
    @Transactional
    public void publishCourse(Long companyId, Long courseId) {
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        // 约束校验
        // 1. 课程审核通过后，方可发布
        if (coursePublishPre == null) {
            XueChengPlusException.cast("请先提交课程审核，审核通过后方可发布");
            return;
        }
        // 2. 本机构只允许发布本机构的课程
        if (!companyId.equals(coursePublishPre.getCompanyId())) {
            XueChengPlusException.cast("本机构只允许发布本机构的课程");
        }
        // 3. 审核通过才可以进行发布
        if (!CourseAuditStatus.PASSED.getCode().equals(coursePublishPre.getStatus())) {
            XueChengPlusException.cast("操作失败，课程审核通过方可发布");
        }
        // 保存到课程发布信息表中
        saveCoursePublish(coursePublishPre);
        // 保存到消息表中
        saveCoursePublishMessage(coursePublishPre);
        // 删除课程预发布表的对应记录
        coursePublishPreMapper.deleteById(courseId);
    }

    /**
     * 课程页面静态化, 生成 html 页面
     *
     * @param courseId 课程 id
     * @return html 文件
     */
    @Override
    public File generateCourseHtml(Long courseId) {
        File htmlFile = null;
        try {
            // 1. 创建一个Freemarker配置
            Configuration configuration = new Configuration(Configuration.getVersion());
            // 2. 告诉Freemarker在哪里可以找到模板文件
            String classPath = Objects.requireNonNull(this.getClass().getResource("/")).getPath();
            configuration.setDirectoryForTemplateLoading(new File(classPath + "/templates/"));
            configuration.setDefaultEncoding("utf-8");
            // 3. 创建一个模型数据，与模板文件中的数据模型保持一致，这里是CoursePreviewDto类型
            CoursePreviewDto coursePreviewInfo = getCoursePreviewInfo(courseId);
            HashMap<String, Object> map = new HashMap<>();
            map.put("model", coursePreviewInfo);
            // 4. 加载模板文件
            Template template = configuration.getTemplate("course_template.ftl");
            // 5. 将数据模型应用于模板
            String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
            InputStream inputStream = IOUtils.toInputStream(content);
            htmlFile = File.createTempFile("course", "html");
            FileOutputStream fileOutputStream = new FileOutputStream(htmlFile);
            IOUtils.copy(inputStream, fileOutputStream);
        } catch (Exception e) {
            log.error("课程静态化失败：{}", e.getMessage());
        }
        return htmlFile;
    }

    /**
     * 上传课程静态化页面
     *
     * @param courseId 课程 id
     * @param file     html 页面
     */
    @Override
    public void uploadCourseHtml(Long courseId, File file) {
        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(file);
        String course = mediaServiceClient.upload(multipartFile, "course", courseId + ".html");
        if (course == null) {
            XueChengPlusException.cast("远程调用媒资服务上传文件失败");
        }
    }

    /**
     * 保存课程索引信息
     *
     * @param courseId 课程 id
     * @return 成功/失败
     */
    @Override
    public Boolean saveCourseIndex(Long courseId) {
        CoursePublish coursePublish = coursePublishMapper.selectById(courseId);
        CourseIndex courseIndex = new CourseIndex();
        BeanUtils.copyProperties(coursePublish, courseIndex);
        Boolean result = searchServiceClient.add(courseIndex);
        if (Boolean.FALSE.equals(result)) {
            XueChengPlusException.cast("添加课程索引失败");
        }
        return true;
    }

    /**
     * 查询课程发布信息
     *
     * @param courseId 课程 id
     * @return 课程发布信息
     */
    @Override
    public CoursePublish getCoursePublish(Long courseId) {
        // TODO: 课程发布信息可建缓存
        return coursePublishMapper.selectById(courseId);
    }

    /**
     * 保存课程发布信息
     *
     * @param coursePublishPre 课程预发布信息
     */
    private void saveCoursePublish(CoursePublishPre coursePublishPre) {
        // 1. 向 course_publish 表中插入一条记录，如果存在则更新，发布状态为已发布
        CoursePublish result = new CoursePublish();
        BeanUtils.copyProperties(coursePublishPre, result);
        result.setStatus(CoursePublishStatus.PUBLISHED.getCode());
        CoursePublish coursePublish = coursePublishMapper.selectById(coursePublishPre.getId());
        if (coursePublish == null) {
            coursePublishMapper.insert(result);
        } else {
            coursePublishMapper.updateById(result);
        }
        // 2. 更新 course_base 表的课程发布状态为已发布
        CourseBase courseBase = courseBaseMapper.selectById(coursePublishPre.getId());
        courseBase.setAuditStatus(CoursePublishStatus.PUBLISHED.getCode());
        courseBaseMapper.updateById(courseBase);
    }

    /**
     * 保存到消息表中
     *
     * @param coursePublishPre 课程预发布信息
     */
    private void saveCoursePublishMessage(CoursePublishPre coursePublishPre) {
        MqMessage mqMessage = mqMessageService.addMessage("course_publish", String.valueOf(coursePublishPre.getId()), null, null);
        if (mqMessage == null) {
            XueChengPlusException.cast("添加到消息表中失败");
        }
    }
}
