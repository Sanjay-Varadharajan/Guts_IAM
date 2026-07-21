package com.guts.Guts_IAM.proxyanalytics.service;


import com.guts.Guts_IAM.proxyanalytics.dto.ApiKeyAnalyticsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class ProxyClientService {

    private final WebClient.Builder webClient;

    public ApiKeyAnalyticsDto getAnalytics(String apiKey){

        return webClient.build()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("http")
                        .host("localhost")
                        .port(9090)
                        .path("/internals/analytics")
                        .queryParam("apiKey", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(ApiKeyAnalyticsDto.class)
                .block();
    }
}
