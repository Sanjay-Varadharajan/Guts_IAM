package com.guts.Guts_IAM.proxyanalytics;

import com.guts.Guts_IAM.common.response.ApiResponse;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@RestController
public class ProxyAnalyticsController {

    private final AnalyticsInternalService analyticsInternalService;

    @GetMapping("/analytics/requests")
    @PreAuthorize("hasRole('ADMIN')")
    public   ResponseEntity<ApiResponse<ApiKeyAnalyticsDto>> proxyAnalytics(@RequestParam String apiKey, Authentication authentication,
                                                                HttpServletRequest httpServletRequest){


        ApiKeyAnalyticsDto apiKeyAnalyticsDto=analyticsInternalService.proxyAnalytics(apiKey,authentication,httpServletRequest);
        ApiResponse<ApiKeyAnalyticsDto> response=new ApiResponse<>(
                true,
                "FETCHING_REQUEST_ANALYTICS",
                apiKeyAnalyticsDto,
                LocalDateTime.now()
        );

        return ResponseEntity.ok(response);
    }

    @PostConstruct
    public void init() {
        System.out.println("analyticsInternalService = " + analyticsInternalService);
    }
}
