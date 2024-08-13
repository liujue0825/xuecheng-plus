package com.xuecheng.learning.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.learning.model.po.XcLearnRecord;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author liujue
 */
public interface XcLearnRecordMapper extends BaseMapper<XcLearnRecord> {

    void initLearnRecord(String userId, Long courseId, Long teachplanId);
}

