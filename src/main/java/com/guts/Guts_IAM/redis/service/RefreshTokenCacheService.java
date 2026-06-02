package com.guts.Guts_IAM.redis.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RefreshTokenCacheService {

    private final RedisTemplate<String,Object> redisTemplate;

    private static final String REFRESH_PREFIX = "refresh:";
    private static final String USER_TOKENS_PREFIX = "user_tokens:";

    public void save(String refreshToken,
                     Integer userId,
                     long ttlMillis){

        redisTemplate.opsForValue().set(
                REFRESH_PREFIX + refreshToken,
                userId,
                ttlMillis,
                TimeUnit.MILLISECONDS
        );

        redisTemplate.opsForSet().add(
                USER_TOKENS_PREFIX + userId,
                refreshToken
        );
    }

    public Integer getUserId(String refreshToken){

        Object value =
                redisTemplate.opsForValue()
                        .get(REFRESH_PREFIX + refreshToken);

        if(value == null){
            return null;
        }

        return Integer.valueOf(value.toString());
    }

    public void deleteToken(String refreshToken){

        redisTemplate.delete(
                REFRESH_PREFIX + refreshToken
        );
    }

    public void removeTokenFromUser(
            Integer userId,
            String refreshToken){

        redisTemplate.opsForSet().remove(
                USER_TOKENS_PREFIX + userId,
                refreshToken
        );
    }

    public Set<Object> getUserTokens(
            Integer userId){

        return redisTemplate.opsForSet()
                .members(
                        USER_TOKENS_PREFIX + userId
                );
    }

    public void deleteUserTokenSet(
            Integer userId){

        redisTemplate.delete(
                USER_TOKENS_PREFIX + userId
        );
    }
}