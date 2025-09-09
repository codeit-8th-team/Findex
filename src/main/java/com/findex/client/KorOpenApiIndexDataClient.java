package com.findex.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.findex.config.OpenApiProperties;
import com.findex.entity.IndexInfo;
import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
public class KorOpenApiIndexDataClient implements IndexDataClient {

  private static final DateTimeFormatter YYYYMMDD = DateTimeFormatter.ofPattern("yyyyMMdd", Locale.KOREA);

  private final OpenApiProperties props;
  private final RestClient rest;
  private final ObjectMapper om = new ObjectMapper();

  @Override
  public IndexDataSnapshot fetch(IndexInfo target) {
    String op = resolveOperation(target);
    LocalDate queryDate = LocalDate.now();

    URI uri = buildUri(op, target.getIndexName(), queryDate);

    String raw = rest.get()
        .uri(uri)                 // baseUrl + path 합쳐져 호출됨
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .body(String.class);

    // 응답 파싱
    try {
      JsonNode root = om.readTree(raw);
      String resultCode = text(root, "/response/header/resultCode");
      String resultMsg  = text(root, "/response/header/resultMsg");

      if (!"00".equals(resultCode)) {
        // 공공데이터 표준 에러 코드 해석(요약)
        throw new IllegalStateException("OpenAPI error: code=" + resultCode + ", msg=" + resultMsg);
      }

      // 가장 첫 번째 item
      JsonNode item = root.at("/response/body/items/item");
      if (item.isMissingNode() || item.isNull()) {
        // 페이지/필터 때문에 없을 수 있음
        log.warn("[OPEN-API] no items for indexName='{}', date={}", target.getIndexName(), queryDate);
        return new IndexDataSnapshot(null, null, null, queryDate);
      }

      // 필드 매핑
      Integer epyItmsCnt = intOrNull(item.get("epyItmsCnt"));
      LocalDate basPntm  = dateOrNull(item.get("basPntm"));   // 기준시점(yyyyMMdd)
      Integer basIdx     = intOrNull(item.get("basIdx"));     // 기준지수
      LocalDate basDt    = dateOrNull(item.get("basDt"));     // 기준일자(yyyyMMdd)

      return new IndexDataSnapshot(epyItmsCnt, basPntm, basIdx, basDt != null ? basDt : queryDate);

    } catch (Exception e) {
      // 네트워크/파싱/에러코드 모두 여기로
      log.error("[OPEN-API] fetch failed for indexName='{}' op={} cause={}",
          target.getIndexName(), op, e.toString());
      throw new RuntimeException(e);
    }
  }

  private URI buildUri(String op, String indexName, LocalDate date) {
    // resultType=json / serviceKey는 대부분 "이미 URL 인코딩된 문자열"이므로 build(true)
    return UriComponentsBuilder.fromPath("/" + op)
        .queryParam("serviceKey", props.serviceKey())
        .queryParam("resultType", "json")
        .queryParam("pageNo", 1)
        .queryParam("numOfRows", 1)
        .queryParam("idxNm", indexName)
        .queryParam("basDt", YYYYMMDD.format(date))
        .build(true)   // already-encoded values respected
        .toUri();
  }

  private static String resolveOperation(IndexInfo idx) {
    String csf = (idx.getIndexClassification() == null ? "" : idx.getIndexClassification()).toLowerCase(Locale.ROOT);
    // 아주 단순한 라우팅 규칙 (필요시 index_info에 sourceType 등으로 더 명확히 스위칭)
    if (csf.contains("채권") || csf.contains("bond")) return "getBondMarketIndex";
    if (csf.contains("파생") || csf.contains("deriv")) return "getDerivationProductMarketIndex";
    return "getStockMarketIndex";
  }

  private static Integer intOrNull(JsonNode n) {
    if (n == null || n.isNull() || n.asText().isBlank()) return null;
    String s = n.asText().trim();
    if (s.contains(".")) s = s.substring(0, s.indexOf('.')); // "2770.69" → "2770"
    try { return Integer.valueOf(s); } catch (Exception e) { return null; }
  }

  private static LocalDate dateOrNull(JsonNode n) {
    if (n == null || n.isNull() || n.asText().isBlank()) return null;
    String s = n.asText().replaceAll("\\s+", "");
    if (s.length() == 8 && s.chars().allMatch(Character::isDigit)) {
      return LocalDate.of(
          Integer.parseInt(s.substring(0,4)),
          Integer.parseInt(s.substring(4,6)),
          Integer.parseInt(s.substring(6,8))
      );
    }
    return null;
  }

  private static String text(JsonNode root, String jsonPointer) {
    JsonNode n = root.at(jsonPointer);
    return n.isMissingNode() || n.isNull() ? null : n.asText();
  }
}