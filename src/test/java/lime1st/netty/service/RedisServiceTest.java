package lime1st.netty.service;

import lime1st.netty.redis.RedisService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

class RedisServiceTest {

    private static ClassPathXmlApplicationContext context;

    private static RedisService redisService;

    @BeforeAll
    static void setUp() {
        context = new ClassPathXmlApplicationContext("redisConfig.xml");
        redisService = context.getBean("redisService", RedisService.class);
    }

    @AfterAll
    static void tearDown() {
        if (context != null) context.close();
    }

    @Test
    public void testCachedAndFind() {
        // 데이터 저장
        redisService.save("user:1", "alex");
        System.out.println("Saved to Redis: user:1 = alex");

        // 데이터 조회
        String value = redisService.get("user:1");
        System.out.println("Retrieved from Redis: " + value);
    }
}