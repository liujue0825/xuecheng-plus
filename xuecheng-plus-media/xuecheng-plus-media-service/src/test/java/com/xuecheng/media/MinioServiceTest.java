package com.xuecheng.media;

import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author liujue
 */
@SpringBootTest
public class MinioServiceTest {
    // 创建MinioClient对象
    static MinioClient minioClient =
            MinioClient.builder()
                    .endpoint("http://192.168.101.129:9000")
                    .credentials("minioadmin", "minioadmin")
                    .build();

    /**
     * 文件目录
     */
    public static final String FILE_PREFIX =  "D:\\CSLearning\\project\\xucheng-plus\\resoures\\media-test\\";

    /**
     * 文件后缀
     */
    public static final String FILE_SUFFIX = ".png";

    /**
     * 上传测试方法
     */
    @Test
    public void uploadTest() {

    }

    public static void main(String[] args) {
        try {
            minioClient.uploadObject(
                    UploadObjectArgs.builder()
                            .bucket("test")
                            .object("pic01.png")    // 同一个桶内对象名不能重复
                            .filename(FILE_PREFIX + "1" + FILE_SUFFIX)
                            .build()
            );
            System.out.println("上传成功");
        } catch (Exception e) {
            System.out.println("上传失败");
        }
    }
}
