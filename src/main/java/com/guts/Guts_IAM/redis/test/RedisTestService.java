package com.guts.Guts_IAM.redis.test;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisTestService {

    private final RedisTemplate<String,Object> template;

    public void testRedis(){
        template.opsForValue().set(
                "hello",
                "bro",
                Duration.ofMinutes(5)
        );

        Object value=template.opsForValue().get("hello");

        System.out.print(value);
    }
}
