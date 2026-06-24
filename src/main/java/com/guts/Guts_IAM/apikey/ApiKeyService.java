package com.guts.Guts_IAM.apikey;


import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApiKeyService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String PREFIX = "api_key:";

    public String generateApiKey(String owner) {

        String apiKey = "guts_" + UUID.randomUUID();

        redisTemplate.opsForValue().set(PREFIX + apiKey, "ACTIVE");

        redisTemplate.opsForValue().set(PREFIX + apiKey + ":owner", owner);

        return apiKey;
    }

    public void revokeApiKey(String apiKey) {
        redisTemplate.delete(PREFIX + apiKey);
        redisTemplate.delete(PREFIX + apiKey + ":owner");
    }

    public boolean exists(String apiKey) {
        return Boolean.TRUE.equals(
                redisTemplate.hasKey(PREFIX + apiKey)
        );
    }
}