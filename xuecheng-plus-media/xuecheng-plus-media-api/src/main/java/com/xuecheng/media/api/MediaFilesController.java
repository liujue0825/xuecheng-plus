package com.xuecheng.media.api;

import com.xuecheng.base.constants.ResourceType;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.service.MediaFileService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @author liujue
 * @description 媒资文件管理接口
 */
@Api(value = "媒资文件管理接口", tags = "媒资文件管理接口")
@RestController
public class MediaFilesController {

    private final MediaFileService mediaFileService;

    @Autowired
    public MediaFilesController(MediaFileService mediaFileService) {
        this.mediaFileService = mediaFileService;
    }

    /**
     * 媒资列表查询接口
     */
    @PostMapping("/files")
    public PageResult<MediaFiles> list(PageParams pageParams, @RequestBody QueryMediaParamsDto queryMediaParamsDto) {
        Long companyId = 1232141425L;
        return mediaFileService.queryMediaFiles(companyId, pageParams, queryMediaParamsDto);

    }

    /**
     * 上传文件接口
     */
    @RequestMapping(value = "/upload/coursefile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadFileResultDto upload(@RequestPart("filedata") MultipartFile upload,
                                      @RequestParam(value = "folder", required = false) String folder,
                                      @RequestParam(value = "objectName", required = false) String objectName) {
        UploadFileParamsDto uploadFileParamsDto = new UploadFileParamsDto();
        uploadFileParamsDto.setFileSize(upload.getSize());
        String contentType = upload.getContentType();
        if ("image".equals(contentType)) {
            // 图片: 001001
            uploadFileParamsDto.setFileType(ResourceType.IMAGE.getCode());
        } else if ("video".equals(contentType)) {
            // 视频: 001002
            uploadFileParamsDto.setContentType(ResourceType.VIDEO.getCode());
        } else {
            // 其他: 001003
            uploadFileParamsDto.setContentType(ResourceType.OTHER.getCode());
        }
        uploadFileParamsDto.setFilename(upload.getOriginalFilename());
        uploadFileParamsDto.setContentType(contentType);
        Long companyId = 1232141425L;
        try {
            return mediaFileService.uploadFile(companyId, uploadFileParamsDto, upload.getBytes(), folder, objectName);
        } catch (IOException e) {
            throw new XueChengPlusException("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 预览文件接口
     */
    @GetMapping("/preview/{mediaId}")
    public RestResponse<String> getPlayUrlByMediaId(@PathVariable String mediaId) {
        MediaFiles mediaFiles = mediaFileService.getFileById(mediaId);
        return RestResponse.success(mediaFiles.getUrl());
    }
}
