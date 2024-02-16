package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.base.utils.MediaUtil;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileService;
import io.minio.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import static com.xuecheng.base.utils.MediaUtil.getContentType;

/**
 * @author liujue
 */
@Slf4j
@Service
public class MediaFileServiceImpl implements MediaFileService {
    @Resource
    private MediaFilesMapper mediaFilesMapper;
    @Resource
    private MediaProcessMapper mediaProcessMapper;
    @Resource
    private MinioClient minioClient;

    /**
     * 代理对象
     */
    @Resource
    private MediaFileService proxy;

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

    /**
     * 媒资文件分页查询
     *
     * @param companyId           机构 id
     * @param pageParams          分页参数
     * @param queryMediaParamsDto 媒资文件查询请求参数
     * @return 分页查询结果
     */
    @Override
    public PageResult<MediaFiles> queryMediaFiles(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {
        LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(queryMediaParamsDto.getFilename()), MediaFiles::getFilename, queryMediaParamsDto.getFilename());
        queryWrapper.eq(StringUtils.isNotEmpty(queryMediaParamsDto.getFileType()), MediaFiles::getFileType, queryMediaParamsDto.getFileType());

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
    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, byte[] bytes, String folder, String objectName) {
        String fileMd5 = DigestUtils.md5DigestAsHex(bytes);
        // 若不存在目录则在桶中新建一个目录
        if (StringUtils.isEmpty(folder)) {
            folder = getFileFolder(true, true, true);
        } else if (!folder.endsWith("/")) {
            // 如果不是目录格式, 则改为目录格式
            folder = folder + "/";
        }
        // 如果对象名为空则需要生成对象名
        if (StringUtils.isEmpty(objectName)) {
            String filename = uploadFileParamsDto.getFilename();
            objectName = getObjectName(filename, fileMd5);
        }
        // 桶中存储对象的完整 url
        objectName = folder + objectName;
        // 将文件上传到 MinIO 中并将文件信息保存到 media_file 表中
        try {
            addMediaFilesToMinio(bytes, bucketFiles, objectName);
            MediaFiles mediaFiles = proxy.addMediaFilesToDb(companyId, uploadFileParamsDto, folder, objectName, fileMd5);
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
    @Transactional
    @Override
    public MediaFiles addMediaFilesToDb(Long companyId, UploadFileParamsDto uploadFileParamsDto, String folder, String objectName, String fileMd5) {
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
            // 1 表示正常
            mediaFiles.setStatus("1");
            mediaFiles.setFilePath(objectName);
            // 如果是图片/mp4 格式就设置 url, 其他类型在文件处理时设置
            if ("image".equals(contentType) || "mp4".equals(contentType)) {
                mediaFiles.setUrl("/" + bucketFiles + "/" + objectName);
            }
            // 002003 表示审核通过
            mediaFiles.setAuditStatus("002003");
        }
        int row = mediaFilesMapper.insert(mediaFiles);
        if (row <= 0) {
            XueChengPlusException.cast("保存文件信息失败");
        }
        // 如果是 avi 视频, 则额外添加到视频待处理表中
        if ("video/x-msvideo".equals(contentType)) {
            MediaProcess mediaProcess = new MediaProcess();
            BeanUtils.copyProperties(mediaFiles, mediaProcess);
            // 1 表示未处理
            mediaProcess.setStatus("1");
            int i = mediaProcessMapper.insert(mediaProcess);
            if (i <= 0) {
                XueChengPlusException.cast("保存avi视频信息失败");
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
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
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

    private String getChunkFileFolderPath(String fileMd5) {
        return fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/" + "chunk" + "/";
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
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        try {
            addMediaFilesToMinio(bytes, video, chunkFileFolderPath);
            return RestResponse.success();
        } catch (Exception e) {
            log.debug("上传分块文件：{}失败：{}", chunkFileFolderPath, e.getMessage());
        }
        return RestResponse.fail();
    }

    /**
     * 合并分块文件
     *
     * @param companyId           机构 id
     * @param fileMd5             文件 md5 值
     * @param chunkTotal          分块总数
     * @param uploadFileParamsDto 文件上传请求类
     * @return true/false
     */
    @Override
    public RestResponse<Boolean> mergeChunk(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto) {
        // 1. 合并文件
        // 获取分块文件
        File[] chunkFile = downloadChunkFile(fileMd5, chunkTotal);
        // 获取源文件相关内容
        String filename = uploadFileParamsDto.getFilename();
        String extension = filename.substring(filename.lastIndexOf("."));
        // 创建临时文件用于合并
        File mergeFile = null;
        try {
            mergeFile = File.createTempFile(filename, extension);
        } catch (IOException e) {
            XueChengPlusException.cast("创建临时文件失败");
        }
        try {
            byte[] buffer = new byte[1024];
            // 写入流
            try (RandomAccessFile writeStream = new RandomAccessFile(mergeFile, "w")) {
                for (File file : chunkFile) {
                    // 读取流
                    try (RandomAccessFile readStream = new RandomAccessFile(file, "r")) {
                        int len;
                        while ((len = readStream.read(buffer)) != -1) {
                            writeStream.write(buffer, 0, len);
                        }
                    } catch (Exception e) {
                        XueChengPlusException.cast("读取文件时出错");
                    }

                }
            } catch (Exception e) {
                XueChengPlusException.cast("合并文件时出错");
            }
            uploadFileParamsDto.setFileSize(mergeFile.length());
            // 2. 校验文件
            try (FileInputStream fis = new FileInputStream(mergeFile)) {
                String mergeMd5 = DigestUtils.md5DigestAsHex(fis);
                if (!fileMd5.equals(mergeMd5)) {
                    XueChengPlusException.cast("合并文件校验失败");
                }
                log.debug("合并文件校验通过:{}", mergeFile.getAbsolutePath());
            } catch (Exception e) {
                XueChengPlusException.cast("合并文件校验过程中出现异常");
            }
            // 3. 上传文件
            String mergeFilePath = getFilePathByMd5(fileMd5, extension);
            addMediaFilesToMinio(mergeFile.getAbsolutePath(), video, mergeFilePath);
            MediaFiles mediaFiles = proxy.addMediaFilesToDb(companyId, uploadFileParamsDto, mergeFilePath, mergeFile.getName(), fileMd5);
            if (mediaFiles == null) {
                XueChengPlusException.cast("媒资文件入库失败");
            }
            log.debug("媒资文件入库成功");
            return RestResponse.success();
        } finally {
            // 删除临时文件
            for (File file : chunkFile) {
                try {
                    file.delete();
                } catch (Exception ignored) {
                }
            }
            try {
                mergeFile.delete();
            } catch (Exception ignored) {
            }
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
        }
        if (StringUtils.isEmpty(mediaFiles.getUrl())) {
            XueChengPlusException.cast("视频还没有转码处理");
        }
        return mediaFiles;
    }

    /**
     * 将文件信息添加到 Minio 中
     *
     * @param bytes      字节数组
     * @param bucket     桶名称
     * @param objectName 对象名称
     */
    private void addMediaFilesToMinio(byte[] bytes, String bucket, String objectName) {
        String contentType = MediaUtil.getContentType(objectName);
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes)) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .stream(byteArrayInputStream, byteArrayInputStream.available(), -1)
                    .contentType(contentType)
                    .build());
            log.debug("上传到文件系统成功");
        } catch (Exception e) {
            log.debug("上传到文件系统出错:{}", e.getMessage());
            throw new XueChengPlusException("上传到文件系统出错");
        }
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
     * 新建 MinIO 中桶内的一个目录
     * 生成规则为: 根据 年/月/日 的格式来生成
     *
     * @param year  是否包含年
     * @param month 是否包含月
     * @param day   是否包含日
     * @return 文件夹
     */
    private String getFileFolder(boolean year, boolean month, boolean day) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        StringBuilder stringBuilder = new StringBuilder();
        String data = dateFormat.format(new Date());
        String[] split = data.split("-");
        if (year) {
            stringBuilder.append(split[0]).append("/");
        }
        if (month) {
            stringBuilder.append(split[1]).append("/");
        }
        if (day) {
            stringBuilder.append(split[2]).append("/");
        }
        return stringBuilder.toString();
    }

    /**
     * 生成存储在 MinIO 中的文件名
     * 生成规则: 文件的 md5 码 + 文件后缀名
     *
     * @param filename 文件名
     * @param md5      文件的 md5 值
     * @return 文件名
     */
    private String getObjectName(String filename, String md5) {
        return md5 + filename.substring(filename.lastIndexOf("."));
    }

    /**
     * 根据 md5 值和文件扩展名生成文件路径
     *
     * @param fileMd5
     * @param extension
     * @return
     */
    private String getFilePathByMd5(String fileMd5, String extension) {
        return fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/" + fileMd5 + extension;
    }

    /**
     * 从 Minio 中下载分块文件
     *
     * @param fileMd5
     * @param chunkTotal
     * @return
     */
    private File[] downloadChunkFile(String fileMd5, int chunkTotal) {
        File[] result = new File[chunkTotal];
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        for (int i = 0; i < chunkTotal; i++) {
            String chunkFilePath = chunkFileFolderPath + i;
            File chunkFile = null;
            try {
                // 创建临时文件
                chunkFile = File.createTempFile("chunk" + i, null);
            } catch (Exception e) {
                XueChengPlusException.cast("创建临时文件失败:" + e.getMessage());
            }
            chunkFile = downloadFromMinio(chunkFile, video, chunkFilePath);
            result[i] = chunkFile;
        }
        return result;
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
}
