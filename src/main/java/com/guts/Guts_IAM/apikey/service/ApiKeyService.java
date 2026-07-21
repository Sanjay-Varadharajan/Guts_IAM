package com.guts.Guts_IAM.apikey.service;


import com.guts.Guts_IAM.apikey.model.ApiKey;
import com.guts.Guts_IAM.apikey.status.Status;
import com.guts.Guts_IAM.apikey.repo.ApikeyRepository;
import com.guts.Guts_IAM.auditlog.action.Action;
import com.guts.Guts_IAM.auditlog.action.AuditStatus;
import com.guts.Guts_IAM.auditlog.service.AuditLogService;
import com.guts.Guts_IAM.common.exception.types.HandleMissingParamException;
import com.guts.Guts_IAM.common.exception.types.UserNameNotFoundException;
import com.guts.Guts_IAM.common.mail.EmailService;
import com.guts.Guts_IAM.geolocation.dto.GeoLocation;
import com.guts.Guts_IAM.geolocation.service.GeoIPService;
import com.guts.Guts_IAM.security.util.hashutil.HashUtil;
import com.guts.Guts_IAM.user.model.User;
import com.guts.Guts_IAM.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ApiKeyService {

    private final RedisTemplate<String, String> redisTemplate;

    private final AuditLogService auditLogService;

    private final ApikeyRepository apikeyRepository;

    private final UserRepository userRepository;

    private final EmailService emailService;

    private final GeoIPService geoIPService;

    private static final String PREFIX = "api_key:";



                    public String generateApiKey(String owner, HttpServletRequest httpServletRequest) throws HandleMissingParamException {


                        if(owner==null || owner.isBlank()){
                            throw new HandleMissingParamException(
                                    "Required Param is Missing",
                                    "BAD_REQUEST",
                                    HttpStatus.BAD_REQUEST
                            );
                        }


                        User user=userRepository.findByUserMailAndActiveTrue(owner).orElseThrow(
                                ()->new UserNameNotFoundException(
                                        "user not found",
                                        "NOT_FOUND",
                                        HttpStatus.NOT_FOUND
                                )
                        );
                        ApiKey apiKeyObj=new ApiKey();
                        apiKeyObj.setUserId(user.getUserId());




                        String apiKey = "guts_" + UUID.randomUUID();

                        apiKeyObj.setHashedApiKey(HashUtil.sha256(apiKey));
                        apikeyRepository.save(apiKeyObj);

                        redisTemplate.opsForValue().set(PREFIX + apiKey, Status.ACTIVE.name());

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

                        String ip=auditLogService.extractIp(httpServletRequest);
                        GeoLocation geo = geoIPService.getLocation(ip);

                        emailService.apiKeyMail(owner,user.getUserName(),ip,geo);

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
