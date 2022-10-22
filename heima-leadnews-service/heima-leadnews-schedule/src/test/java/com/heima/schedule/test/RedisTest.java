package com.heima.schedule.test;

import com.heima.common.redis.CacheService;
import com.heima.schedule.ScheduleApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Set;

@SpringBootTest(classes = ScheduleApplication.class)
@RunWith(SpringRunner.class)
public class RedisTest {
    @Autowired
    private CacheService cacheService;

    @Test
    public void testList(){
//        cacheService.lLeftPush("list_001","hello,redis");
        String list_001 = cacheService.lRightPop("list_001");
        System.out.println(list_001);
    }

    @Test
    public void testZset(){
        cacheService.zAdd("zset_key_001","001", 1000L);
        cacheService.zAdd("zset_key_001","002", 8888L);
        cacheService.zAdd("zset_key_001","003", 7777L);
        cacheService.zAdd("zset_key_001","004", 1L);

        Set<String> zset_key_001 = cacheService.zRange("zset_key_001", 0L, 8888L);

    }
}
