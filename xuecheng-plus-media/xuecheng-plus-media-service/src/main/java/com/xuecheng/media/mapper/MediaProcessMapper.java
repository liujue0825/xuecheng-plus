package com.xuecheng.media.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.media.model.po.MediaProcess;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author liujue
 */
public interface MediaProcessMapper extends BaseMapper<MediaProcess> {

    @Select("SELECT * FROM media_process WHERE id % #{shardTotal} = #{shardIndex} AND status = '1' LIMIT #{count}")
    List<MediaProcess> selectListByShardIndex(@Param("shardIndex") int shardIndex,
                                              @Param("shardTotal") int shardTotal,
                                              @Param("count") int count);
}
