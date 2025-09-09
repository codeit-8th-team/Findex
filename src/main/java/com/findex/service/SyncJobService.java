package com.findex.service;


import com.findex.client.IndexDataClient;
import com.findex.client.IndexDataSnapshot;
import com.findex.dto.sync.IndexInfosSyncRequest;
import com.findex.dto.sync.SyncJobDto;
import com.findex.entity.IndexInfo;
import com.findex.entity.SyncJob;
import com.findex.enums.JobResult;
import com.findex.enums.JobType;
import com.findex.mapper.SyncJobMapper;
import com.findex.repository.IndexInfoRepository;
import com.findex.repository.SyncJobRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SyncJobService {

  private final IndexInfoRepository indexInfoRepository;
  private final SyncJobRepository syncJobRepository;
  private final IndexDataClient indexDataClient;
  private final SyncJobMapper syncJobMapper;

  @Transactional
  public List<SyncJobDto> syncIndexInfos(IndexInfosSyncRequest req, HttpServletRequest http) {
    List<Long> ids = req.indexInfoIds();
    List<IndexInfo> targets = indexInfoRepository.findAllById(ids);
    if (targets.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "대상 지수가 없습니다.");

    String workerIp = clientIp(http);
    Instant now = Instant.now();
    List<SyncJob> jobs = new ArrayList<>(targets.size());

    for (IndexInfo idx : targets) {
      JobResult result;
      LocalDate targetDate; // ← 블록 바깥에 선언

      try {
        IndexDataSnapshot snap = indexDataClient.fetch(idx);

        boolean changed = false;
        if (snap.employedItemsCount() != null && !Objects.equals(idx.getEmployedItemsCount(), snap.employedItemsCount())) {
          idx.setEmployedItemsCount(snap.employedItemsCount());
          changed = true;
        }
        if (snap.basePointInTime() != null && !Objects.equals(idx.getBasePointInTime(), snap.basePointInTime())) {
          idx.setBasePointInTime(snap.basePointInTime());
          changed = true;
        }
        if (snap.baseIndex() != null && !Objects.equals(idx.getBaseIndex(), snap.baseIndex())) {
          idx.setBaseIndex(snap.baseIndex());
          changed = true;
        }

        targetDate = (snap.baseDate() != null ? snap.baseDate() : LocalDate.now());
        result = JobResult.SUCCESS;

      } catch (Exception e) {
        result = JobResult.FAILED;
        targetDate = LocalDate.now();
      }

      jobs.add(SyncJob.of(
          idx,
          JobType.INDEX_INFO,
          targetDate,
          workerIp,
          now,
          result
      ));
    }

    return syncJobRepository.saveAll(jobs).stream().map(syncJobMapper::toDto).toList();
  }

  private static String clientIp(HttpServletRequest req) {
    // XFF 우선, 없으면 remoteAddr
    String xff = req.getHeader("X-Forwarded-For");
    if (xff != null && !xff.isBlank()) {
      // 첫번째 IP
      int comma = xff.indexOf(',');
      return (comma > 0) ? xff.substring(0, comma).trim() : xff.trim();
    }
    return req.getRemoteAddr();
  }
}