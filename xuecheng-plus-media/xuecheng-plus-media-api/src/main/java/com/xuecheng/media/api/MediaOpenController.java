package com.xuecheng.media.api;

import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.service.MediaFileService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 *
 * @author liujue
 */
@RestController
@RequestMapping("/open")
public class MediaOpenController {
    @Resource
    private MediaFileService mediaFileService;

    /**
     * 根据课程计划获取视频地址接口
     */
    @GetMapping("/preview/{mediaId}")
    public RestResponse<String> getMediaUrl(@PathVariable String mediaId) {
        MediaFiles mediaFiles = mediaFileService.getFileById(mediaId);
        if (mediaFiles == null || StringUtils.isEmpty(mediaFiles.getUrl())) {
            XueChengPlusException.cast("视频未处理");
        }
        return RestResponse.success(mediaFiles.getUrl());
    }
}
