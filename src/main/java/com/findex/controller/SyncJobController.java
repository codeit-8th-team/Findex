package com.findex.controller;

import com.findex.dto.sync.IndexInfosSyncRequest;
import com.findex.dto.sync.SyncJobDto;
import com.findex.service.SyncJobService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sync-jobs")
@Tag(name = "3. 연동 작업 API")
public class SyncJobController {

  private final SyncJobService syncJobService;

  @PostMapping("/index-infos")
  @ResponseStatus(HttpStatus.ACCEPTED) // 202
  public List<SyncJobDto> syncIndexInfos(@RequestBody @Valid IndexInfosSyncRequest req,
      HttpServletRequest http) {
    return syncJobService.syncIndexInfos(req, http);
  }
}