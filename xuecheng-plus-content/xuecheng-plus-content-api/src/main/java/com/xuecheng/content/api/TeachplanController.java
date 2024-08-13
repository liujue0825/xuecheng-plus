package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.service.TeachplanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author liujue
 */
@Slf4j
@RestController
@Api(value = "课程计划编辑接口", tags = "课程计划编辑接口")
public class TeachplanController {

    private final TeachplanService teachplanService;

    @Autowired
    public TeachplanController(TeachplanService teachplanService) {
        this.teachplanService = teachplanService;
    }

    /**
     * 查询课程计划树形结构
     */
    @GetMapping("/teachplan/{courseId}/tree-nodes")
    public List<TeachplanDto> getTreeNodes(@PathVariable Long courseId) {
        return teachplanService.findTeachplanTree(courseId);
    }

    /**
     * 获取教学计划信息
     */
    @PostMapping("/content/teachplan/{teachplanId}")
    public Teachplan getTeachplan(@PathVariable Long teachplanId) {
        return teachplanService.getTeachPlanById(teachplanId);
    }

    @ApiOperation("课程计划创建或修改")
    @PostMapping("/teachplan")
    public void saveTeachplan(@RequestBody Teachplan teachplanDto) {
        teachplanService.saveTeachplan(teachplanDto);
    }

    @ApiOperation("课程计划删除")
    @DeleteMapping("/teachplan/{teachplanId}")
    public void deleteTeachplan(@PathVariable Long teachplanId) {
        teachplanService.deleteTeachplan(teachplanId);
    }

    @ApiOperation("课程计划排序")
    @PostMapping("/teachplan/{moveType}/{teachplanId}")
    public void orderByTeachplan(@PathVariable String moveType, @PathVariable Long teachplanId) {
        teachplanService.orderByTeachplan(moveType, teachplanId);
    }

    /**
     * 课程计划与媒资信息绑定接口
     */
    @PostMapping("/teachplan/association/media")
    public void associationMedia(@RequestBody BindTeachplanMediaDto dto) {
        teachplanService.associationMedia(dto);
    }

    /**
     * 课程计划与媒资信息解除绑定接口
     */
    @DeleteMapping("/teachplan/association/media/{teachplanId}/{mediaId}")
    public void unassociationMedia(@PathVariable Long teachplanId, @PathVariable Long mediaId) {
        teachplanService.unassociationMedia(teachplanId, mediaId);
    }
}
