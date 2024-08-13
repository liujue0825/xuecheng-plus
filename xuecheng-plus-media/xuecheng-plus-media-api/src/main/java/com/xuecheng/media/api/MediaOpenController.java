package com.xuecheng.media.api;

import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.service.MediaFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * 视频文件预览
 *
 * @author liujue
 */
@RestController
@RequestMapping("/open")
public class MediaOpenController {

    private final MediaFileService mediaFileService;

    @Autowired
    public MediaOpenController(MediaFileService mediaFileService) {
        this.mediaFileService = mediaFileService;
    }

    /**
     * 根据课程计划获取视频地址
     */
    @GetMapping("/preview/{mediaId}")
    public RestResponse<String> getMediaUrl(@PathVariable String mediaId) {
        MediaFiles mediaFiles = mediaFileService.getFileById(mediaId);
        return RestResponse.success(mediaFiles.getUrl());
    }
}
