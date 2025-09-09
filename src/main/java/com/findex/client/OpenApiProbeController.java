package com.findex.client;

import com.findex.entity.IndexInfo;
import com.findex.repository.IndexInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

//연결확인 메서드 나중에 삭제
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/_probe")
public class OpenApiProbeController {

  private final IndexDataClient client;
  private final IndexInfoRepository repo;

  // 예: GET /api/_probe/index-snapshot?id=1
  @GetMapping("/index-snapshot")
  public IndexDataSnapshot snapshot(@RequestParam Long id) {
    IndexInfo info = repo.getOrThrow(id);
    return client.fetch(info);
  }
}