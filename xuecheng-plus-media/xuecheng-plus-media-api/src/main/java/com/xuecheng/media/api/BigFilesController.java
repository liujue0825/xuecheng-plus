package com.xuecheng.media.api;

import com.xuecheng.base.constants.ResourceType;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.service.MediaFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 大文件上传接口
 *
 * @author liujue
 */
@RestController
public class BigFilesController {

    private final MediaFileService mediaFileService;

    @Autowired
    public BigFilesController(MediaFileService mediaFileService) {
        this.mediaFileService = mediaFileService;
    }

    /**
     * 大文件上传前检查文件
     */
    @PostMapping("/upload/checkfile")
    public RestResponse<Boolean> checkFile(@RequestParam("fileMD5") String fileMd5) {
        return mediaFileService.checkFile(fileMd5);
    }

    /**
     * 分块文件上传前检查分块
     */
    @PostMapping("/upload/checkchunk")
    public RestResponse<Boolean> checkChunk(@RequestParam("fileMd5") String fileMd5, @RequestParam("chunk") int chunk) {
        return mediaFileService.checkChunk(fileMd5, chunk);
    }

    /**
     * 上传分块文件
     */
    @PostMapping("/upload/uploadchunk")
    public RestResponse<Boolean> uploadChunk(@RequestParam("file") MultipartFile file,
                                             @RequestParam("fileMd5") String fileMd5,
                                             @RequestParam("chunk") int chunk) {

        try {
            return mediaFileService.uploadChunk(fileMd5, chunk, file.getBytes());
        } catch (IOException e) {
            XueChengPlusException.cast(e.getMessage());
        }
        return null;
    }

    /**
     * 合并分块文件
     */
    @PostMapping("/upload/mergechunks")
    public RestResponse<Boolean> mergeChunks(@RequestParam("fileMd5") String fileMd5,
                                             @RequestParam("fileName") String fileName,
                                             @RequestParam("chunkTotal") int chunkTotal) {
        Long companyId = 1232141425L;
        UploadFileParamsDto uploadFileParamsDto = new UploadFileParamsDto();
        uploadFileParamsDto.setFileType(ResourceType.VIDEO.getCode());
        uploadFileParamsDto.setTags("课程视频");
        uploadFileParamsDto.setRemark("");
        uploadFileParamsDto.setFilename(fileName);
        return mediaFileService.mergeChunk(companyId, fileMd5, chunkTotal, uploadFileParamsDto);
    }
}
