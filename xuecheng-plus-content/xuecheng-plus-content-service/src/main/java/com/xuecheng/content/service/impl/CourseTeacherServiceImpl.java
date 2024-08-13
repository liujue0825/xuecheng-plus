package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.CourseTeacherMapper;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author liujue
 */
@Slf4j
@Service
public class CourseTeacherServiceImpl implements CourseTeacherService {

    private final CourseTeacherMapper courseTeacherMapper;

    @Autowired
    public CourseTeacherServiceImpl(CourseTeacherMapper courseTeacherMapper) {
        this.courseTeacherMapper = courseTeacherMapper;
    }

    @Override
    public List<CourseTeacher> getCourseTeacherList(Long courseId) {
        // SELECT * FROM course_teacher WHERE course_id = 117
        LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getCourseId, courseId);
        return courseTeacherMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional
    public CourseTeacher saveCourseTeacher(CourseTeacher courseTeacher) {
        Long id = courseTeacher.getId();
        CourseTeacher teacher;
        if (id == null) {
            // id 为 null，新增教师
            teacher = new CourseTeacher();
            BeanUtils.copyProperties(courseTeacher, teacher);
            teacher.setCreateDate(LocalDateTime.now());
            int flag = courseTeacherMapper.insert(teacher);
            if (flag <= 0) {
                XueChengPlusException.cast("新增失败");
            }
        } else {
            // id 不为 null，修改教师
            teacher = courseTeacherMapper.selectById(id);
            BeanUtils.copyProperties(courseTeacher, teacher);
            int flag = courseTeacherMapper.updateById(teacher);
            if (flag <= 0) {
                XueChengPlusException.cast("修改失败");
            }
        }
        return courseTeacherMapper.selectById(teacher.getId());
    }

    @Override
    public void deleteCourseTeacher(Long courseId, Long teacherId) {
        LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getId, teacherId);
        queryWrapper.eq(CourseTeacher::getCourseId, courseId);
        int row = courseTeacherMapper.delete(queryWrapper);
        if (row < 0) {
            XueChengPlusException.cast("删除失败");
        }
    }
}
