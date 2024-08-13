package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessHistoryMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.model.po.MediaProcessHistory;
import com.xuecheng.media.service.MediaFileProcessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author liujue
 */
@Slf4j
@Service
public class MediaFileProcessServiceImpl implements MediaFileProcessService {

    private final MediaFilesMapper mediaFilesMapper;

    private final MediaProcessMapper mediaProcessMapper;

    private final MediaProcessHistoryMapper mediaProcessHistoryMapper;

    @Autowired
    public MediaFileProcessServiceImpl(MediaFilesMapper mediaFilesMapper,
                                       MediaProcessMapper mediaProcessMapper,
                                       MediaProcessHistoryMapper mediaProcessHistoryMapper) {
        this.mediaFilesMapper = mediaFilesMapper;
        this.mediaProcessMapper = mediaProcessMapper;
        this.mediaProcessHistoryMapper = mediaProcessHistoryMapper;
    }

    /**
     * 获取待处理任务列表
     * <p>
     * 每个执行器只处理指定分片序号的任务
     * </p>
     *
     * @param shardIndex 分片序号
     * @param shardTotal 分片总数
     * @param count      任务总数
     * @return 待处理任务列表
     */
    @Override
    public List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count) {
        return mediaProcessMapper.selectListByShardIndex(shardIndex, shardTotal, count);
    }

    /**
     * 更新任务状态
     * <p>
     * 根据 status=1/2/3 进行处理
     * 2: 任务执行成功，则更新文件表中视频的 URL、及任务处理结果，将待处理任务记录删除，同时向历史任务表添加记录
     * 3: 任务处理失败，则设置失败原因
     * </p>
     *
     * @param taskId   任务 id
     * @param status   状态
     * @param fileId   文件标识
     * @param url      访问地址
     * @param errorMsg 异常信息
     */
    @Override
    @Transactional
    public void saveProcessFinishStatus(Long taskId, String status, String fileId, String url, String errorMsg) {
        MediaProcess mediaProcess = mediaProcessMapper.selectById(taskId);
        if (mediaProcess == null) {
            log.warn("更新任务状态时此任务 {} 不存在!", fileId);
            return;
        }
        LambdaQueryWrapper<MediaProcess> queryWrapper =
                new LambdaQueryWrapper<MediaProcess>().eq(MediaProcess::getId, taskId);
        // 任务执行成功
        if ("2".equals(status)) {
            // 更新 mediaProcess 内容
            mediaProcess.setStatus("2");
            mediaProcess.setUrl(url);
            mediaProcess.setFinishDate(LocalDateTime.now());
            // 补充上 media_files 表中 url 字段
            MediaFiles mediaFiles = mediaFilesMapper.selectById(fileId);
            if (mediaFiles != null) {
                mediaFiles.setUrl(url);
                mediaFilesMapper.updateById(mediaFiles);
            }
            // 添加到 media_process_history 表
            MediaProcessHistory mediaProcessHistory = new MediaProcessHistory();
            BeanUtils.copyProperties(mediaProcess, mediaProcessHistory);
            mediaProcessHistoryMapper.insert(mediaProcessHistory);
            // 删除 media_process 表中处理完的记录
            mediaProcessMapper.deleteById(mediaProcess.getId());
        }
        // 任务执行失败
        if ("3".equals(status)) {
            log.debug("任务执行失败: {}", errorMsg);
            MediaProcess errorMediaProcess = new MediaProcess();
            errorMediaProcess.setStatus("3");
            errorMediaProcess.setErrormsg(errorMsg);
            errorMediaProcess.setFinishDate(LocalDateTime.now());
            mediaProcessMapper.update(errorMediaProcess, queryWrapper);
        }
    }
}
