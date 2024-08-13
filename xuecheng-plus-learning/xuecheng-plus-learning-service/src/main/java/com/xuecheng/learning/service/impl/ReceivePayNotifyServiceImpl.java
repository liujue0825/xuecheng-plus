package com.xuecheng.learning.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.base.constants.BusinessOrderType;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.learning.config.MQConfig;
import com.xuecheng.learning.service.MyCourseTablesService;
import com.xuecheng.learning.service.ReceivePayNotifyService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author liujue
 * @version 1.0
 * @description: 订单发送服务类
 * @date 2024/2/20 17:11
 */
@Slf4j
@Service
public class ReceivePayNotifyServiceImpl implements ReceivePayNotifyService {

    private final MyCourseTablesService myCourseTablesService;

    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public ReceivePayNotifyServiceImpl(MyCourseTablesService myCourseTablesService, RabbitTemplate rabbitTemplate) {
        this.myCourseTablesService = myCourseTablesService;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = MQConfig.PAYNOTIFY_QUEUE)
    @Transactional
    public void receive(Message message) {
        // 1. 获取消息
        MqMessage mqMessage = JSON.parseObject(message.getBody(), MqMessage.class);
        // 2. 根据我们存入的消息，进行解析
        // 2.1 消息类型，学习中心只处理支付结果的通知
        String messageType = mqMessage.getMessageType();
        // 2.2 选课id
        String chooseCourseId = mqMessage.getBusinessKey1();
        // 2.3 订单类型，60201 表示购买课程，学习中心只负责处理这类订单请求
        String orderType = mqMessage.getBusinessKey2();
        // 3. 学习中心只负责处理支付结果的通知
        if (MQConfig.MESSAGE_TYPE.equals(messageType) &&
                BusinessOrderType.COURSE_PURCHASE.getCode().equals(orderType)) {
            // 3.2 保存选课记录
            boolean flag = myCourseTablesService.saveChooseCourseStatus(chooseCourseId);
            if (!flag) {
                XueChengPlusException.cast("保存选课记录失败");
            }
        }
        send(mqMessage);
    }

    @Override
    public void send(MqMessage mqMessage) {
        // 转 json
        String msg = JSON.toJSONString(mqMessage);
        // 发送消息
        rabbitTemplate.convertAndSend(MQConfig.PAYNOTIFY_REPLY_QUEUE, msg);
        log.info("学习中心服务向订单服务回复消息：{}", mqMessage);
    }
}
