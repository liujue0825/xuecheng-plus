package com.xuecheng.media.model.dto;

import com.xuecheng.media.model.po.MediaFiles;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 上传文件响应结果类
 * <p>
 * 单独定义以便于扩展
 * </p>
 *
 * @author liujue
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ToString
public class UploadFileResultDto extends MediaFiles {

}
