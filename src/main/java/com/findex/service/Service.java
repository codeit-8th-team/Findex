package com.findex.service;

import com.findex.config.OpenApiProperties;
import lombok.RequiredArgsConstructor;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class Service {

  private final OpenApiProperties props; // 생성자 주입

  public String callSomething() {
    String baseUrl = props.baseUrl();
    String serviceKey = props.serviceKey(); // 여기서 사용
    // ... 호출 코드
    return "...";
  }
}