package com.xuecheng.media.service;

import com.xuecheng.media.model.po.MediaProcess;

import java.util.List;

/**
 * 媒资文件处理相关业务
 *
 * @author liujue
 */
public interface MediaFileProcessService {

    /**
     * 获取待处理任务列表
     *
     * @param shardIndex 分片序号
     * @param shardTotal 分片总数
     * @param count      任务总数
     * @return 待处理任务列表
     */
    List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count);

    /**
     * 更新任务状态
     *
     * @param taskId   任务 id
     * @param status   状态
     * @param fileId   文件标识
     * @param url      访问地址
     * @param errorMsg 异常信息
     */
    void saveProcessFinishStatus(Long taskId, String status, String fileId, String url, String errorMsg);
}
