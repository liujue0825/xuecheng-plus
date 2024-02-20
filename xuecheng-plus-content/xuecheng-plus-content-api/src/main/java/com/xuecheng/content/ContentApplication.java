package com.xuecheng.content;

import com.spring4all.swagger.EnableSwagger2Doc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author liujue
 */
@EnableSwagger2Doc
@EnableFeignClients(basePackages={"com.xuecheng.*.feignclient"})
@SpringBootApplication(scanBasePackages = "com.xuecheng")
public class ContentApplication {

    public static void main(String[] args) {
        SpringApplication.run(ContentApplication.class, args);
    }

}
