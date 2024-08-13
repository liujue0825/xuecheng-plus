package com.xuecheng.content.test;

import com.xuecheng.content.config.MultipartSupportConfig;
import com.xuecheng.content.feignclient.MediaServiceClient;
import com.xuecheng.content.service.CoursePublishService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;

/**
 * @author liujue
 */
@SpringBootTest
public class FeignClientTest {
    @Resource
    MediaServiceClient mediaServiceClient;

    //远程调用，上传文件
    @Test
    public void test() {
        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(new File("D:\\test11.html"));
        String result = mediaServiceClient.upload(multipartFile, "course", "test.html");
        System.out.println(result);
    }
}
