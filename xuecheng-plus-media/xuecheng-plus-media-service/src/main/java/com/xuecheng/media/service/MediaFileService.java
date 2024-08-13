package com.xuecheng.media.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;

import java.io.File;

/**
 * @author liuejue
 * @description 媒资文件管理业务类
 */
public interface MediaFileService {

    /**
     * 媒资文件分页查询
     *
     * @param companyId           机构 id
     * @param pageParams          分页参数
     * @param queryMediaParamsDto 媒资文件查询请求参数
     * @return 分页查询结果
     */
    PageResult<MediaFiles> queryMediaFiles(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto);

    /**
     * 上传图片文件
     *
     * @param companyId           机构 id
     * @param uploadFileParamsDto 上传文件请求参数类
     * @param bytes               字节数组
     * @param folder              桶下面的子目录
     * @param objectName          对象名称
     * @return 上传文件响应类
     */
    UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, byte[] bytes, String folder, String objectName);

    /**
     * 将文件信息添加到数据库中
     *
     * @param companyId           机构 id
     * @param uploadFileParamsDto 上传文件请求参数类
     * @param folder              桶下面的子目录
     * @param objectName          对象名称
     * @param fileMd5             源完整文件的 md5 值
     */
    MediaFiles addMediaFilesToDb(Long companyId, UploadFileParamsDto uploadFileParamsDto, String folder, String objectName, String fileMd5);

    /**
     * 将文件信息添加到 Minio 中
     *
     * @param filePath   文件路径
     * @param bucket     桶
     * @param objectName 对象名称
     */
    void addMediaFilesToMinio(String filePath, String bucket, String objectName);

    /**
     * 从 Minio 中下载文件
     *
     * @param file       空白文件
     * @param bucket     桶
     * @param objectName 对象名称
     * @return 目标文件
     */
    File downloadFromMinio(File file, String bucket, String objectName);

    /**
     * 检查文件是否存在于数据库和 Minio 中
     *
     * @param fileMd5 源完整文件的 md5 值
     * @return true/false
     */
    RestResponse<Boolean> checkFile(String fileMd5);

    /**
     * 检查当前分块是否存在于 Minio 中
     *
     * @param fileMd5    源完整文件的 md5 值
     * @param chunkIndex 当前分块序号
     * @return true/false
     */
    RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex);

    /**
     * 上传分块文件到 Minio 中
     *
     * @param fileMd5    源完整文件的 md5 值
     * @param chunkIndex 当前分块序号
     * @param bytes      文件字节数组
     * @return true/false
     */
    RestResponse<Boolean> uploadChunk(String fileMd5, int chunkIndex, byte[] bytes);

    /**
     * 合并分块文件
     *
     * @param companyId           机构 id
     * @param fileMd5             源完整文件的 md5 值
     * @param chunkTotal          分块总数
     * @param uploadFileParamsDto 文件上传请求类
     * @return true/false
     */
    RestResponse<Boolean> mergeChunk(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto);

    /**
     * 查询媒资文件
     * <p>
     * 注: 如果视频还未转码处理同样查询不到
     * </p>
     *
     * @param mediaId 文件 id
     * @return 媒资文件
     */
    MediaFiles getFileById(String mediaId);
}
