package com.guts.Guts_IAM.apikey;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController("/api/v1/admin")
public class ApiKeyController {


    private final ApiKeyService apiKeyService;

    @PostMapping("/generate")
    public ResponseEntity<String> generate(
            @RequestParam String owner
    ) {
        String key = apiKeyService.generateApiKey(owner);
        return ResponseEntity.ok(key);
    }

    @DeleteMapping("/revoke")
    public ResponseEntity<String> revoke(
            @RequestParam String apiKey
    ) {
        apiKeyService.revokeApiKey(apiKey);
        return ResponseEntity.ok("API key revoked");
    }

    @GetMapping("/exists")
    public ResponseEntity<Boolean> exists(
            @RequestParam String apiKey
    ) {
        return ResponseEntity.ok(apiKeyService.exists(apiKey));
    }
}

