package com.xuecheng.media.service.jobhandler;

import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.utils.Mp4VideoUtil;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileProcessService;
import com.xuecheng.media.service.MediaFileService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.*;

/**
 * 视频处理任务
 *
 * @author liujue
 */
@Slf4j
@Component
public class VideoTask {
    public static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();

    /**
     * ffmpeg 的安装位置
     */
    @Value("${videoprocess.ffmpegpath}")
    private String ffmpegPath;

    @Resource
    private MediaFileService mediaFileService;

    @Resource
    private MediaFileProcessService mediaFileProcessService;

    /**
     * 视频处理任务, 处理逻辑为：
     * 1. 根据分片序号和分片总数，查询待处理任务
     * 2. 启动多线程去处理
     * 3. 将原始视频下载到本地
     * 4. 调用工具类将 avi 转为 mp4
     * 5. 上传到 MinIO
     * 6. 记录任务处理结果 url
     */
    @XxlJob("videoJobHandler")
    public void videoJobHandler() throws InterruptedException {
        // 1. 根据分片序号和分片总数，查询待处理任务
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        List<MediaProcess> mediaProcessList = mediaFileProcessService.getMediaProcessList(shardIndex, shardTotal, CPU_COUNT);
        if (CollectionUtils.isEmpty(mediaProcessList)) {
            log.debug("当前待处理任务数为 0");
            return;
        }
        // 2. 启动多线程去处理
        int size = mediaProcessList.size();
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(size, size, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
        CountDownLatch countDownLatch = new CountDownLatch(size);
        mediaProcessList.forEach(mediaProcess -> threadPoolExecutor.execute(() -> {
            String status = mediaProcess.getStatus();
            // 需要避免重复任务
            if ("2".equals(status)) {
                log.debug("该视频已经被处理，无需再次处理。视频信息：{}", mediaProcess);
                return;
            }
            // 3. 将原始视频从 Minio 中下载到本地
            File originalFile = null;
            File mp4File = null;
            try {
                String bucket = mediaProcess.getBucket();
                String filePath = mediaProcess.getFilePath();
                String fileId = mediaProcess.getFileId();
                try {
                    originalFile = File.createTempFile("original", null);
                    mp4File = File.createTempFile("mp4", ".mp4");
                } catch (IOException e) {
                    XueChengPlusException.cast("处理视频前创建临时文件失败");
                }
                try {
                    mediaFileService.downloadFromMinio(originalFile, bucket, filePath);
                } catch (Exception e) {
                    log.error("下载原始文件过程中出错：{}，文件信息：{}", e.getMessage(), mediaProcess);
                    XueChengPlusException.cast("下载原始文件失败");
                }
                // 4. 调用工具类将 avi 转为 mp4
                String result = null;
                try {
                    Mp4VideoUtil videoUtil = new Mp4VideoUtil(ffmpegPath, originalFile.getAbsolutePath(), mp4File.getName(), mp4File.getAbsolutePath());
                    result = videoUtil.generateMp4();
                } catch (Exception e) {
                    log.error("处理视频失败，视频地址：{}，错误信息：{}", originalFile.getAbsolutePath(), e.getMessage());
                    XueChengPlusException.cast("处理视频失败");
                }

                // 5. 上传到 MinIO
                // 默认状态为 “3”
                status = "3";
                String url = null;
                if ("success".equals(result)) {
                    String objectName = getFilePathByMd5(fileId, ".mp4");
                    try {
                        mediaFileService.addMediaFilesToMinio(mp4File.getAbsolutePath(), bucket, objectName);
                    } catch (Exception e) {
                        log.error("上传文件失败：{}", e.getMessage());
                        XueChengPlusException.cast("上传文件失败");
                    }
                    status = "2";
                    url = "/" + bucket + "/" + objectName;
                }
                // 6. 记录任务处理结果 url
                mediaFileProcessService.saveProcessFinishStatus(mediaProcess.getId(), status, fileId, url, result);
            } finally {
                // 清理文件
                log.debug("清理临时文件");
                try {
                    if (originalFile != null) {
                        originalFile.delete();
                    }
                    if (mp4File != null) {
                        mp4File.delete();
                    }
                } catch (Exception ignored) {
                }
                // 计数器减一
                countDownLatch.countDown();
            }
        }));
        // 等待,给一个充裕的超时时间,防止无限等待，到达超时时间还没有处理完成则结束任务
        countDownLatch.await(30, TimeUnit.MINUTES);
    }

    /**
     * 根据 md5 值和文件扩展名生成文件路径
     */
    private String getFilePathByMd5(String fileMd5, String extension) {
        return fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/" + fileMd5 + extension;
    }
}
