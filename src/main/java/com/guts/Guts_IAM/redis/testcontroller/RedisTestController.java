package com.guts.Guts_IAM.redis.testcontroller;


import com.guts.Guts_IAM.redis.test.RedisTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RedisTestController {

    private final RedisTestService redisTestService;

    @GetMapping("/rest-test")
    public String testRedis(){
        redisTestService.testRedis();
        return "OK";
    }
}
