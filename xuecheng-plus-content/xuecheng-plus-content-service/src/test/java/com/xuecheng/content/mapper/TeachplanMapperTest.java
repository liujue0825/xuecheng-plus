package com.xuecheng.content.mapper;

import com.xuecheng.content.model.po.Teachplan;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


/**
 * @author liujue
 */
@Slf4j
@SpringBootTest
class TeachplanMapperTest {

    @Autowired
    TeachplanMapper teachplanMapper;

    @Test
    void test() {
        Teachplan teachplan = teachplanMapper.selectPrevTeachplan(26L, 1, 4);
        System.out.println(teachplan);
    }
}
