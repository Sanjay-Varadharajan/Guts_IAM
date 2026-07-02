package com.guts.Guts_IAM.apikey;


import com.guts.Guts_IAM.auditlog.action.Action;
import com.guts.Guts_IAM.auditlog.action.AuditStatus;
import com.guts.Guts_IAM.auditlog.service.AuditLogService;
import com.guts.Guts_IAM.common.exception.types.HandleMissingParamException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ApiKeyService {

    private final RedisTemplate<String, String> redisTemplate;

    private final AuditLogService auditLogService;

    private static final String PREFIX = "api_key:";



    public String generateApiKey(String owner, HttpServletRequest httpServletRequest) throws HandleMissingParamException {


        if(owner==null || owner.isBlank()){
            throw new HandleMissingParamException(
                    "Required Param is Missing",
                    "BAD_REQUEST",
                    HttpStatus.BAD_REQUEST
            );
        }

        String apiKey = "guts_" + UUID.randomUUID();

        redisTemplate.opsForValue().set(PREFIX + apiKey, "ACTIVE");

        redisTemplate.opsForValue().set(PREFIX + apiKey + ":owner", owner);

        auditLogService.log(
                null,
                Action.GENERATED_AUDIT_LOG,
                "API_KEY",
                owner,
                AuditStatus.SUCCESS,
                "api key is generated successfully",
                httpServletRequest
        );


        return apiKey;
    }

    public void revokeApiKey(String apiKey,HttpServletRequest httpServletRequest    ) {
        redisTemplate.delete(PREFIX + apiKey);
        redisTemplate.delete(PREFIX + apiKey + ":owner");
        auditLogService.log(
                null,
                Action.REVOKE_API_KEY,
                "API_KEY",
                apiKey,
                AuditStatus.SUCCESS,
                "api key is revoked successfully",
                httpServletRequest
        );
    }

    public boolean exists(String apiKey) {
        return Boolean.TRUE.equals(
                redisTemplate.hasKey(PREFIX + apiKey)
        );
    }

    public Map<String,Object> getallKeys(HttpServletRequest httpServletRequest) {

        Set<String> keys = redisTemplate.keys(PREFIX + "*");

        Map<String, Object> response = new HashMap<>();

        if (keys != null) {
            for (String key : keys) {
                response.put(key, redisTemplate.opsForValue().get(key));
            }
        }

        auditLogService.log(
                null,
                Action.VIEW_API_KEYS,
                "API_KEY",
                httpServletRequest.getRequestId(),
                AuditStatus.SUCCESS,
                "api key is viewed successfully",
                httpServletRequest

        );

        return response;
    }

}
