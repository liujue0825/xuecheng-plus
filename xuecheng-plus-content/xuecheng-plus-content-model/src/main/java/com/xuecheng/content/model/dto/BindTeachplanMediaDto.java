package com.xuecheng.content.model.dto;

import lombok.Data;

/**
 * 教学计划-媒资绑定请求类
 * @author liujue
 */
@Data
public class BindTeachplanMediaDto {

    /**
     * 文件 id
     */
    private String mediaId;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 教学计划 id
     */
    private long teachplanId;
}
