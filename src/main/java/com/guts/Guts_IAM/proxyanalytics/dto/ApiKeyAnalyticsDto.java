package com.guts.Guts_IAM.proxyanalytics.dto;

import lombok.Data;

@Data
public class ApiKeyAnalyticsDto {

    private Long apiAnalyticsId;
    private String apiKey;
    private Long totalRequests;
    private Long successfulRequests;
    private Long failedRequests;
}