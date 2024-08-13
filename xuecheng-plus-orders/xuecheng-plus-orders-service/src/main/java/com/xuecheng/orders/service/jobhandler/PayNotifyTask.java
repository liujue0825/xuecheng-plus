package com.xuecheng.orders.service.jobhandler;

import com.alibaba.fastjson.JSON;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MessageProcessAbstract;
import com.xuecheng.messagesdk.service.MqMessageService;
import com.xuecheng.orders.config.MQConfig;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 支付通知任务, 保证分布式事务一致性
 *
 * @author liujue
 * @version 1.0
 * @since 2024/8/3
 */
@Slf4j
@Component
public class PayNotifyTask extends MessageProcessAbstract {

    private final RabbitTemplate rabbitTemplate;

    private final MqMessageService mqMessageService;

    @Autowired
    public PayNotifyTask(RabbitTemplate rabbitTemplate, MqMessageService mqMessageService) {
        this.rabbitTemplate = rabbitTemplate;
        this.mqMessageService = mqMessageService;
    }

    @XxlJob("NotifyPayResultJobHandler")
    public void notifyPayResultJobHandler() {
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        process(shardIndex, shardTotal, MQConfig.MESSAGE_TYPE, 100, 60);
    }

    /**
     * 任务处理
     *
     * @param mqMessage 任务内容
     * @return 执行结果
     */
    @Override
    public boolean execute(MqMessage mqMessage) {
        // 向消息队列中发送支付结果通知消息
        send(mqMessage);
        return false;
    }

    /**
     * 接收回复
     * <p>监听支付结果通过回复队列
     *
     * @param message 消息内容
     */
    @RabbitListener(queues = MQConfig.PAYNOTIFY_REPLY_QUEUE)
    public void receive(String message) {
        log.debug("收到支付结果通知回复：{}", message);
        MqMessage mqMessage = JSON.parseObject(message, MqMessage.class);
        // 完成消息发送，最终该消息删除了
        mqMessageService.completed(mqMessage.getId());
    }


    /**
     * 发送支付结果通知
     *
     * @param message 消息内容
     */
    private void send(MqMessage message) {
        String jsonStringMsg = JSON.toJSONString(message);
        // 开始发送消息,使用 fanout 交换机，通过广播模式发送消息
        rabbitTemplate.convertAndSend(MQConfig.PAYNOTIFY_EXCHANGE_FANOUT, "", jsonStringMsg);
        log.debug("向消息队列发送支付结果通知消息完成：{}", message);
    }
}
