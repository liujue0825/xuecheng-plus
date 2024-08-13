package com.xuecheng.content;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author liujue
 */
@EnableFeignClients(basePackages={"com.xuecheng.content.feignclient"})
@SpringBootApplication(scanBasePackages = "com.xuecheng")
public class ContentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ContentServiceApplication.class, args);
    }

}
