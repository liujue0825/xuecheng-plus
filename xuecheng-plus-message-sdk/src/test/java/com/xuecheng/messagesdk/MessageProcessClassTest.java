package com.xuecheng.messagesdk;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * @author liujue
 */
@SpringBootTest
public class MessageProcessClassTest {
    @Resource
    private MessageProcessClass messageProcessClass;

    @Test
    public void test() {
        System.out.println("开始执行-----》" + LocalDateTime.now());
        messageProcessClass.process(0, 1, "test", 2, 10);
        System.out.println("结束执行-----》" + LocalDateTime.now());
    }
}
