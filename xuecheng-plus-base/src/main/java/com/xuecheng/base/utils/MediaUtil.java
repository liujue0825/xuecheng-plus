package com.xuecheng.base.utils;

import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import org.springframework.http.MediaType;

/**
 * 媒资文件相关工具类
 *
 * @author liujue
 * @version 1.0
 */
public class MediaUtil {

    private MediaUtil() {}

    /**
     * 根据文件扩展名得到 content-type
     *
     * @param objectName 文件扩展名
     * @return 对应的 content-type
     */
    public static String getContentType(String objectName) {
        String contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE; // 默认 content-type 为未知二进制流
        // 判断对象名是否包含 .
        if (objectName.contains(".")) {
            // 有 .  则划分出扩展名
            String extension = objectName.substring(objectName.lastIndexOf("."));
            // 根据扩展名得到 content-type，如果为未知扩展名，例如 .abc 之类的东西，则会返回 null
            ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
            // 如果得到了正常的 content-type，则重新赋值，覆盖默认类型
            if (extensionMatch != null) {
                contentType = extensionMatch.getMimeType();
            }
        }
        return contentType;
    }

}
