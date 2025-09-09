package com.findex.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {
  @Bean
  public RestClient restClient(OpenApiProperties props) {
    // baseUrl을 미리 주입
    return RestClient.builder()
        .baseUrl(
            props.baseUrl())  // e.g. https://apis.data.go.kr/1160100/service/GetMarketIndexInfoService
        .build();
  }
}
