package com.xuecheng.media.utils;

import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.utils.MediaUtil;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * MinIO 相关操作的工具类
 *
 * @author liujue
 */
@Slf4j
public class MinIOUtils {

    private MinIOUtils () {

    }

    /**
     * 生成存储在 MinIO 中的文件名
     * 生成规则: 文件的 md5 码 + 文件后缀名
     *
     * @param filename 文件名
     * @param md5      文件的 md5 值
     * @return 文件名
     */
    public static String getObjectName(String filename, String md5) {
        return md5 + filename.substring(filename.lastIndexOf("."));
    }

    public static String getChunkFileFolderPath(String fileMd5) {
        return fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/" + "chunk" + "/";
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
    public static String getFileFolder(boolean year, boolean month, boolean day) {
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
     * 将文件信息添加到 Minio 中
     *
     * @param bytes      字节数组
     * @param bucket     桶名称
     * @param objectName 对象名称
     */
    public static void addMediaFilesToMinio(byte[] bytes, String bucket, String objectName, MinioClient minioClient) {
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
            log.debug("上传到文件系统出错: {}", e.getMessage());
            throw new XueChengPlusException("上传到文件系统出错");
        }
    }
}
