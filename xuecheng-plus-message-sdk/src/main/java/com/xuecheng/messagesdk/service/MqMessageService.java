package com.xuecheng.messagesdk.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.messagesdk.model.po.MqMessage;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author liujue
 */
public interface MqMessageService extends IService<MqMessage> {

    /**
     * 查询消息列表
     *
     * @param shardIndex  分片序号
     * @param shardTotal  分片总数
     * @param messageType 消息类型
     * @param count       扫描记录数
     * @return 消息记录列表
     */
    List<MqMessage> getMessageList(int shardIndex, int shardTotal, String messageType, int count);

    /**
     * 添加消息
     *
     * @param messageType  消息类型
     * @param businessKey1 业务 id
     * @param businessKey2 业务 id
     * @param businessKey3 业务 id
     * @return 消息类
     */
    MqMessage addMessage(String messageType, String businessKey1, String businessKey2, String businessKey3);

    /**
     * 完成任务
     *
     * @param id 消息 id
     * @return 更新结果: 1 为成功
     */
    int completed(long id);

    /**
     * 完成阶段任务
     *
     * @param id 消息 id
     * @return 更新结果: 1 为成功
     */
    int completedStageOne(long id);

    int completedStageTwo(long id);

    int completedStageThree(long id);

    int completedStageFour(long id);

    /**
     * 查询阶段任务处理状态
     *
     * @param id 消息 id
     * @return 更新结果: 1 为成功
     */
    int getStageOne(long id);

    int getStageTwo(long id);

    int getStageThree(long id);

    int getStageFour(long id);

}
