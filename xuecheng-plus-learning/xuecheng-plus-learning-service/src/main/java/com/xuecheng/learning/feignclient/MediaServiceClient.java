package com.xuecheng.learning.feignclient;

import com.xuecheng.base.model.RestResponse;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 媒资服务远程调用接口
 *
 * @author liujue
 */
@FeignClient(value = "media-api", fallbackFactory = MediaServiceClient.MediaServiceClientFallbackFactory.class)
@RequestMapping("/media")
public interface MediaServiceClient {

    /**
     * 根据媒资 id 获取对应视频的 url
     *
     * @param mediaId 媒资 id
     * @return 视频的 url
     */
    @GetMapping("/open/preview/{mediaId}")
    RestResponse<String> getPlayUrlByMediaId(@PathVariable("mediaId") String mediaId);

    @Slf4j
    class MediaServiceClientFallbackFactory implements FallbackFactory<MediaServiceClient> {

        @Override
        public MediaServiceClient create(Throwable throwable) {
            return mediaId -> {
                log.error("远程调用媒资管理服务熔断异常：{}", throwable.getMessage());
                return null;
            };
        }
    }
}
