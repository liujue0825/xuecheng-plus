package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.media.mapper.MediaProcessHistoryMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.model.po.MediaProcessHistory;
import com.xuecheng.media.service.MediaFileProcessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author liujue
 */
@Slf4j
@Service
public class MediaFileProcessServiceImpl implements MediaFileProcessService {
    @Resource
    private MediaProcessMapper mediaProcessMapper;
    @Resource
    private MediaProcessHistoryMapper mediaProcessHistoryMapper;

    /**
     * 获取待处理任务列表
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
     * 根据 status=1/2/3 进行处理
     * 2: 任务执行成功，则更新视频的URL、及任务处理结果，将待处理任务记录删除，同时向历史任务表添加记录
     * 3: 任务处理失败，则设置失败原因
     *
     * @param taskId   任务 id
     * @param status   状态
     * @param fileId   文件标识
     * @param url      访问地址
     * @param errorMsg 异常信息
     */
    @Override
    public void saveProcessFinishStatus(Long taskId, String status, String fileId, String url, String errorMsg) {
        MediaProcess mediaProcess = mediaProcessMapper.selectById(taskId);
        if (mediaProcess == null) {
            return;
        }
        LambdaQueryWrapper<MediaProcess> queryWrapper =
                new LambdaQueryWrapper<MediaProcess>().eq(MediaProcess::getId, taskId);
        // 任务执行成功
        if ("2".equals(status)) {
            mediaProcess.setStatus("2");
            mediaProcess.setUrl(url);
            mediaProcess.setFinishDate(LocalDateTime.now());
            mediaProcessMapper.update(mediaProcess, queryWrapper);
            MediaProcessHistory mediaProcessHistory = new MediaProcessHistory();
            BeanUtils.copyProperties(mediaProcess, mediaProcessHistory);
            mediaProcessHistoryMapper.insert(mediaProcessHistory);
            mediaProcessMapper.deleteById(taskId);
        }
        // 任务执行失败
        if ("3".equals(status)) {
            log.debug("任务执行失败:{}", errorMsg);
            MediaProcess errorMediaProcess = new MediaProcess();
            errorMediaProcess.setStatus("3");
            errorMediaProcess.setErrormsg(errorMsg);
            errorMediaProcess.setFinishDate(LocalDateTime.now());
            mediaProcessMapper.update(errorMediaProcess, queryWrapper);
        }
    }
}
