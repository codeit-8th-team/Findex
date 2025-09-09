package com.findex.dto.sync;

import com.findex.enums.JobResult;
import com.findex.enums.JobType;
import java.time.Instant;
import java.time.LocalDate;

public record SyncJobDto(
    Long id,
    JobType jobType,
    Long indexInfoId,
    String worker,
    LocalDate targetDate,  // ← 추가
    Instant jobTime,
    JobResult result
) {}