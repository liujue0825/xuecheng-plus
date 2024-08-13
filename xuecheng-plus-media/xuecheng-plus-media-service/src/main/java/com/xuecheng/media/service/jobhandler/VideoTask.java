package com.xuecheng.media.service.jobhandler;

import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.utils.FileUtil;
import com.xuecheng.base.utils.Mp4VideoUtil;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileProcessService;
import com.xuecheng.media.service.MediaFileService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

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

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();

    /**
     * ffmpeg 的安装位置
     */
    @Value("${videoprocess.ffmpegpath}")
    private String ffmpegPath;

    private final MediaFileService mediaFileService;

    private final MediaFileProcessService mediaFileProcessService;

    @Autowired
    public VideoTask(MediaFileService mediaFileService, MediaFileProcessService mediaFileProcessService) {
        this.mediaFileService = mediaFileService;
        this.mediaFileProcessService = mediaFileProcessService;
    }

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
        List<MediaProcess> mediaProcessList =
                mediaFileProcessService.getMediaProcessList(shardIndex, shardTotal, CPU_COUNT);
        if (CollectionUtils.isEmpty(mediaProcessList)) {
            log.debug("当前待处理任务数为 0");
            return;
        }
        // 2. 启动多线程去处理
        int size = mediaProcessList.size();
        ThreadPoolExecutor threadPoolExecutor =
                new ThreadPoolExecutor(size, size * 2, 0L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(size * 10));
        CountDownLatch countDownLatch = new CountDownLatch(size);
        mediaProcessList.forEach(mediaProcess ->
                threadPoolExecutor.execute(() ->
                        processVideo(mediaProcess, countDownLatch)));
        // 等待,给一个充裕的超时时间,防止无限等待，到达超时时间还没有处理完成则结束任务
        boolean await = countDownLatch.await(30, TimeUnit.MINUTES);
        if (!await) {
            throw new InterruptedException("The waiting time elapsed before the count reached zero!");
        }
        // 操作完毕关闭线程池以释放资源
        threadPoolExecutor.shutdown();
    }

    /**
     * 视频处理过程抽取
     */
    private void processVideo(MediaProcess mediaProcess, CountDownLatch countDownLatch) {
        String status = mediaProcess.getStatus();
        // 需要保证幂等性, 避免重复任务
        if ("2".equals(status)) {
            log.debug("该视频已经被处理，无需再次处理。视频信息：{}", mediaProcess);
            countDownLatch.countDown();
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
                originalFile = mediaFileService.downloadFromMinio(originalFile, bucket, filePath);
            } catch (Exception e) {
                log.error("下载原始文件过程中出错：{}，文件信息：{}", e.getMessage(), mediaProcess);
                XueChengPlusException.cast("下载原始文件失败");
            }
            // 4. 调用工具类将 avi 转为 mp4
            String mp4Name = fileId + ".mp4";
            String mp4Path = mp4File.getAbsolutePath();
            String result = null;
            try {
                Mp4VideoUtil videoUtil = new Mp4VideoUtil(
                        ffmpegPath, originalFile.getAbsolutePath(), mp4Name, mp4Path);
                result = videoUtil.generateMp4();
            } catch (Exception e) {
                log.error("处理视频失败，视频地址：{}，错误信息：{}", originalFile, e.getMessage());
                XueChengPlusException.cast("处理视频失败");
            }
            // 5. 上传到 MinIO
            status = "3";   // 默认状态为 "3"
            String url = null;
            if ("success".equals(result)) {
                String objectName = FileUtil.getFilePathByMd5(fileId, ".mp4");
                try {
                    mediaFileService.addMediaFilesToMinio(mp4Path, bucket, objectName);
                } catch (Exception e) {
                    log.error("上传文件失败：{}", e.getMessage());
                    XueChengPlusException.cast("上传文件失败");
                }
                status = "2";   // 处理成功, 状态为 "2"
                // 生成 url
                url = "/" + bucket + "/" + objectName;
            }
            // 6. 记录任务处理结果 url
            mediaFileProcessService.saveProcessFinishStatus(mediaProcess.getId(), status, fileId, url, result);
        } finally {
            // 清理文件
            originalFile.delete();
            mp4File.delete();
            log.info("清理临时文件成功");
            countDownLatch.countDown();
        }
    }
}
