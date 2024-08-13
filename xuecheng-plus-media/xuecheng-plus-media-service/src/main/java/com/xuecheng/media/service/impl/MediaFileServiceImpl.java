package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.constants.AuditStatus;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.base.utils.FileUtil;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileService;
import com.xuecheng.media.utils.MinIOUtils;
import io.minio.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.io.*;
import java.time.LocalDateTime;
import java.util.List;

import static com.xuecheng.base.utils.MediaUtil.getContentType;

/**
 * @author liujue
 */
@Slf4j
@Service
public class MediaFileServiceImpl implements MediaFileService {

    private final MediaFilesMapper mediaFilesMapper;

    private final MediaProcessMapper mediaProcessMapper;

    private final MinioClient minioClient;

    /**
     * 代理对象
     */
    private final MediaFileService proxy;

    /**
     * 普通文件存储桶
     */
    @Value("${minio.bucket.files}")
    private String bucketFiles;

    /**
     * 大文件存储桶
     */
    @Value("${minio.bucket.videofiles}")
    private String video;

    @Autowired
    public MediaFileServiceImpl(MediaFilesMapper mediaFilesMapper,
                                MediaProcessMapper mediaProcessMapper,
                                MinioClient minioClient,
                                @Lazy MediaFileService proxy) {
        this.mediaFilesMapper = mediaFilesMapper;
        this.mediaProcessMapper = mediaProcessMapper;
        this.minioClient = minioClient;
        this.proxy = proxy;
    }

    /**
     * 媒资文件分页查询
     *
     * @param companyId           机构 id
     * @param pageParams          分页参数
     * @param queryMediaParamsDto 媒资文件查询请求参数
     * @return 分页查询结果
     */
    @Override
    public PageResult<MediaFiles> queryMediaFiles(Long companyId, PageParams pageParams,
                                                  QueryMediaParamsDto queryMediaParamsDto) {
        LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(queryMediaParamsDto.getFilename()),
                MediaFiles::getFilename, queryMediaParamsDto.getFilename());
        queryWrapper.eq(StringUtils.isNotEmpty(queryMediaParamsDto.getFileType()),
                MediaFiles::getFileType, queryMediaParamsDto.getFileType());

        Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        Page<MediaFiles> result = mediaFilesMapper.selectPage(page, queryWrapper);
        List<MediaFiles> list = result.getRecords();
        long total = result.getTotal();
        return new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
    }

    /**
     * 上传文件的通用接口, 包括:
     * 1. 将文件上传到 MinIO 中
     * 2. 将文件信息保存到 media_file 表中
     *
     * @param companyId           机构 id
     * @param uploadFileParamsDto 上传文件请求参数类
     * @param bytes               字节数组
     * @param folder              桶下面的子目录
     * @param objectName          对象名称
     * @return 上传文件响应类
     */
    @Override
    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto,
                                          byte[] bytes, String folder, String objectName) {
        String fileMd5 = DigestUtils.md5DigestAsHex(bytes);
        // 若不存在目录则在桶中新建一个目录
        if (StringUtils.isEmpty(folder)) {
            folder = MinIOUtils.getFileFolder(true, true, true);
        } else if (!folder.endsWith("/")) {
            // 如果不是目录格式, 则改为目录格式
            folder = folder + "/";
        }
        // 如果对象名为空则需要生成对象名
        if (StringUtils.isEmpty(objectName)) {
            String filename = uploadFileParamsDto.getFilename();
            objectName = MinIOUtils.getObjectName(filename, fileMd5);
        }
        // 桶中存储对象的完整 url
        objectName = folder + objectName;
        // 将文件上传到 MinIO 中并将文件信息保存到 media_file 表中
        try {
            MinIOUtils.addMediaFilesToMinio(bytes, bucketFiles, objectName, minioClient);
            MediaFiles mediaFiles = proxy.addMediaFilesToDb(
                    companyId, uploadFileParamsDto, folder, objectName, fileMd5);
            UploadFileResultDto result = new UploadFileResultDto();
            BeanUtils.copyProperties(mediaFiles, result);
            return result;
        } catch (Exception e) {
            XueChengPlusException.cast("上传过程中出错");
        }
        return null;
    }

    /**
     * 将文件信息添加到数据库
     *
     * @param companyId           机构 id
     * @param uploadFileParamsDto 上传文件请求参数类
     * @param folder              桶下面的子目录
     * @param objectName          对象名称
     * @param fileMd5             文件的 MD5 值
     */
    @Override
    @Transactional
    public MediaFiles addMediaFilesToDb(Long companyId, UploadFileParamsDto uploadFileParamsDto,
                                        String folder, String objectName, String fileMd5) {
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        String contentType = getContentType(objectName);
        if (mediaFiles == null) {
            mediaFiles = new MediaFiles();
            BeanUtils.copyProperties(uploadFileParamsDto, mediaFiles);
            mediaFiles.setId(fileMd5);
            mediaFiles.setFileId(fileMd5);
            mediaFiles.setCompanyId(companyId);
            mediaFiles.setBucket(bucketFiles);
            mediaFiles.setCreateDate(LocalDateTime.now());
            mediaFiles.setStatus("1");
            mediaFiles.setFilePath(objectName);
            // 如果是图片/mp4 格式就设置 url, 其他类型在文件处理时设置
            if ("image".equals(contentType) || "mp4".equals(contentType)) {
                mediaFiles.setUrl("/" + bucketFiles + "/" + objectName);
            }
            mediaFiles.setAuditStatus(AuditStatus.PASSED.getCode());
        }
        int row = mediaFilesMapper.insert(mediaFiles);
        if (row <= 0) {
            XueChengPlusException.cast("保存文件信息失败");
        }
        // 如果是不可预览的视频类型(暂时只处理 avi 类型), 则额外添加到视频待处理表中
        if ("video/x-msvideo".equals(contentType)) {
            MediaProcess mediaProcess = new MediaProcess();
            BeanUtils.copyProperties(mediaFiles, mediaProcess);
            mediaProcess.setStatus("1");
            int i = mediaProcessMapper.insert(mediaProcess);
            if (i <= 0) {
                XueChengPlusException.cast("保存 avi 视频信息失败");
            }
        }
        return mediaFiles;
    }

    /**
     * 检查文件是否存在
     *
     * @param fileMd5 文件 md5 值
     * @return true/false
     */
    @Override
    public RestResponse<Boolean> checkFile(String fileMd5) {
        // 1. 检查数据库中是否存在
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles == null) {
            return RestResponse.fail();
        }
        // 2. 检查 Minio 中对应文件夹下是否存在
        try (GetObjectResponse object = minioClient.getObject(GetObjectArgs.builder()
                .bucket(mediaFiles.getBucket())
                .object(mediaFiles.getFilePath())
                .build())) {
            if (object == null) {
                return RestResponse.fail();
            }
        } catch (Exception e) {
            return RestResponse.fail();
        }
        return RestResponse.success();
    }

    /**
     * 检查当前分块是否存在
     *
     * @param fileMd5    文件 md5 值
     * @param chunkIndex 当前分块序号
     * @return true/false
     */
    @Override
    public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex) {
        String chunkFileFolderPath = MinIOUtils.getChunkFileFolderPath(fileMd5);
        String chunkFilePath = chunkFileFolderPath + chunkIndex;
        try (GetObjectResponse object = minioClient.getObject(GetObjectArgs.builder()
                .bucket(video)
                .object(chunkFilePath)
                .build())) {
            if (object == null) {
                return RestResponse.fail();
            }
        } catch (Exception e) {
            return RestResponse.fail();
        }
        return RestResponse.success();
    }

    /**
     * 上传分块文件
     *
     * @param fileMd5    文件 md5 值
     * @param chunkIndex 当前分块序号
     * @param bytes      文件字节数组
     * @return true/false
     */
    @Override
    public RestResponse<Boolean> uploadChunk(String fileMd5, int chunkIndex, byte[] bytes) {
        String chunkFileFolderPath = MinIOUtils.getChunkFileFolderPath(fileMd5);
        // 单个分片文件的存放位置
        String chunkFilePath = chunkFileFolderPath + chunkIndex;
        try {
            MinIOUtils.addMediaFilesToMinio(bytes, video, chunkFilePath, minioClient);
            return RestResponse.success();
        } catch (Exception e) {
            log.debug("上传分块文件：{} 失败：{}", chunkFileFolderPath, e.getMessage());
        }
        return RestResponse.fail();
    }

    /**
     * 合并分块文件
     *
     * @param companyId           机构 id
     * @param fileMd5             源文件 md5 值
     * @param chunkTotal          分块总数
     * @param uploadFileParamsDto 文件上传请求类
     * @return true/false
     */
    @Override
    public RestResponse<Boolean> mergeChunk(Long companyId, String fileMd5, int chunkTotal,
                                            UploadFileParamsDto uploadFileParamsDto) {


        // 1. 校验
        String filename = uploadFileParamsDto.getFilename();
        File[] chunkFiles = checkChunkStatus(fileMd5, chunkTotal);
        String extension = filename.substring(filename.lastIndexOf("."));
        // 创建临时文件作为合并文件
        File mergeFile;
        try {
            mergeFile = File.createTempFile(fileMd5, extension);
        } catch (Exception e) {
            throw new XueChengPlusException("合并文件过程中创建临时文件出错");
        }
        // 2. 合并
        try {
            // 2.1 写入临时文件
            byte[] buffer = new byte[1024];
            try (RandomAccessFile rafWriter = new RandomAccessFile(mergeFile, "rw")) {
                for (File chunkFile : chunkFiles) {
                    try (FileInputStream fis = new FileInputStream(chunkFile)) {
                        int len;
                        while ((len = fis.read(buffer)) != -1) {
                            // 向合并后的文件写入
                            rafWriter.write(buffer, 0, len);
                        }
                    }
                }
            } catch (IOException e) {
                XueChengPlusException.cast("合并文件过程中出错");
            }
            log.debug("合并文件完成 {}", mergeFile.getAbsolutePath());
            uploadFileParamsDto.setFileSize(mergeFile.length());
            // 2.2 校验文件内容
            try (FileInputStream mergeFis = new FileInputStream(mergeFile)) {
                // 通过 md5 值来校验
                String md5Hex = DigestUtils.md5DigestAsHex(mergeFis);
                if (!fileMd5.equalsIgnoreCase(md5Hex)) {
                    XueChengPlusException.cast("合并文件校验失败");
                }
                log.debug("合并文件校验通过 {}", mergeFile.getAbsolutePath());
            } catch (Exception e) {
                XueChengPlusException.cast("合并文件校验异常");
            }
            // 3. 上传
            // 3.1 临时文件上传到 minio
            String mergeFilepath = FileUtil.getFilePathByMd5(fileMd5, extension);
            try {
                addMediaFilesToMinio(mergeFile.getAbsolutePath(), bucketFiles, mergeFilepath);
                log.debug("合并文件上传 MinIO 完成 {}", mergeFile.getAbsolutePath());
            } catch (Exception e) {
                XueChengPlusException.cast("合并文件时上传文件出错");
            }
            // 3.2 文件信息上传到数据库
            MediaFiles mediaFiles = proxy.addMediaFilesToDb(
                    companyId, uploadFileParamsDto, fileMd5, bucketFiles, mergeFilepath);
            if (mediaFiles == null) {
                XueChengPlusException.cast("媒资文件入库时出错");
            }

            return RestResponse.success();
        } finally {
            // 4. 删除
            for (File chunkFile : chunkFiles) {
                chunkFile.delete();
            }
            mergeFile.delete();
            log.debug("临时文件清理完毕。");
        }
    }

    /**
     * 查询媒资文件
     *
     * @param mediaId 文件 id
     * @return 媒资文件
     */
    @Override
    public MediaFiles getFileById(String mediaId) {
        MediaFiles mediaFiles = mediaFilesMapper.selectById(mediaId);
        if (mediaFiles == null) {
            XueChengPlusException.cast("查询失败");
            return null;
        }
        if (StringUtils.isEmpty(mediaFiles.getUrl())) {
            XueChengPlusException.cast("视频还没有转码处理");
        }
        return mediaFiles;
    }

    @Override
    public void addMediaFilesToMinio(String filePath, String bucket, String objectName) {
        String contentType = getContentType(objectName);
        try {
            minioClient.uploadObject(UploadObjectArgs
                    .builder()
                    .bucket(bucket)
                    .object(objectName)
                    .filename(filePath)
                    .contentType(contentType)
                    .build());
        } catch (Exception e) {
            XueChengPlusException.cast("上传到文件系统出错");
        }
    }

    /**
     * 从 Minio 中下载文件
     *
     * @param file       空白文件
     * @param bucket     桶
     * @param objectName 对象名称
     * @return 目标文件
     */
    @Override
    public File downloadFromMinio(File file, String bucket, String objectName) {
        try (FileOutputStream fileOutputStream = new FileOutputStream(file);
             GetObjectResponse object = minioClient.getObject(GetObjectArgs.builder()
                     .bucket(bucket)
                     .object(objectName)
                     .build())) {
            IOUtils.copy(object, fileOutputStream);
            return file;
        } catch (Exception e) {
            XueChengPlusException.cast("下载文件分块出错");
        }
        return null;
    }


    /**
     * 检查所有分块是否上传完毕并返回所有的分块文件
     *
     * @param fileMd5    文件 md5
     * @param chunkTotal 总共的分块数
     * @return 分块文件数组
     */
    private File[] checkChunkStatus(String fileMd5, int chunkTotal) {
        File[] files = new File[chunkTotal];
        String chunkFileFolderPath = MinIOUtils.getChunkFileFolderPath(fileMd5);
        for (int i = 0; i < chunkTotal; i++) {
            String chunkFilePath = chunkFileFolderPath + i;
            File chunkFile = null;
            try {
                // 创建临时文件
                chunkFile = File.createTempFile("chunk" + i, null);
            } catch (Exception e) {
                XueChengPlusException.cast("下载分块文件时创建临时文件失败: " + e.getMessage());
            }
            chunkFile = downloadFromMinio(chunkFile, video, chunkFilePath);
            files[i] = chunkFile;
        }
        return files;
    }
}
