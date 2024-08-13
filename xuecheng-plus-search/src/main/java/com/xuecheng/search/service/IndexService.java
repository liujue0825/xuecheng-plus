package com.xuecheng.search.service;


/**
 * 课程索引服务
 *
 * @author liujue
 */
public interface IndexService {

    /**
     * 添加课程索引
     *
     * @param indexName 索引名称
     * @param id        id
     * @param object    索引对象
     * @return 成功/失败
     */
    Boolean addCourseIndex(String indexName, String id, Object object);

    /**
     * 更新课程索引
     *
     * @param indexName 索引名称
     * @param id        id
     * @param object    索引对象
     * @return 成功/失败
     */
    Boolean updateCourseIndex(String indexName, String id, Object object);

    /**
     * 删除课程索引
     *
     * @param indexName 索引名称
     * @param id        主键
     * @return 成功/失败
     */
    Boolean deleteCourseIndex(String indexName, String id);
}
