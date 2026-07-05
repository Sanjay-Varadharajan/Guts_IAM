package com.guts.Guts_IAM.apikey;


import com.guts.Guts_IAM.common.exception.types.HandleMissingParamException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
@RestController
        @RequestMapping("/api/v1/key/admin")
public class ApiKeyController {


    private final ApiKeyService apiKeyService;

    @PostMapping("/generate")
    public ResponseEntity<String> generate(
            @RequestParam
            @Email
            String owner,
            HttpServletRequest httpServletRequest
    )throws HandleMissingParamException {
        String key = apiKeyService.generateApiKey(owner,httpServletRequest);
        return ResponseEntity.ok(key);
    }

    @DeleteMapping("/revoke")
    public ResponseEntity<String> revoke(
            @RequestParam  String apiKey,
            HttpServletRequest httpServletRequest
    ) {
        apiKeyService.revokeApiKey(apiKey,httpServletRequest);
        return ResponseEntity.ok("API key revoked");
    }

    @GetMapping("/exists")
    public ResponseEntity<Boolean> exists(
            @RequestParam String apiKey,
            HttpServletRequest httpServletRequest
    ) {
        return ResponseEntity.ok(apiKeyService.exists(apiKey));
    }

    @GetMapping("/apikeys")
    public ResponseEntity<Map<String,Object>> getAllApiKeys(HttpServletRequest httpServletRequest) {


        return ResponseEntity.ok(apiKeyService.getallKeys(httpServletRequest));

    }
}

