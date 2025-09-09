package com.findex.client;

//오류 확인 메서드
public class OpenApiGatewayException extends RuntimeException {
  private final String code;
  private final String providerMessage;
  private final String raw;

  public OpenApiGatewayException(String code, String providerMessage, String raw) {
    super("OpenAPI error: code=" + code + ", msg=" + providerMessage);
    this.code = code;
    this.providerMessage = providerMessage;
    this.raw = raw;
  }
  public String code() { return code; }
  public String providerMessage() { return providerMessage; }
  public String raw() { return raw; }
}
