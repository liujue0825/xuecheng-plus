package com.xuecheng.learning.api;

import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.learning.service.MyLearningService;
import com.xuecheng.learning.util.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;


/**
 * 我的学习相关功能接口
 *
 * @author liujue
 */
@Slf4j
@RestController
public class MyLearningController {

    private final MyLearningService myLearningService;

    @Autowired
    public MyLearningController(MyLearningService myLearningService) {
        this.myLearningService = myLearningService;
    }

    /**
     * 获取视频接口
     */
    @GetMapping("/open/learn/getvideo/{courseId}/{teachplanId}/{mediaId}")
    public RestResponse<String> getVideo(@PathVariable("courseId") Long courseId,
                                         @PathVariable("teachplanId") Long teachplanId,
                                         @PathVariable("mediaId") String mediaId) {
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if (user == null) {
            XueChengPlusException.cast("请登录后继续选课");
            return null;
        }
        return myLearningService.getVideo(user.getId(), courseId, teachplanId, mediaId);
    }
}
