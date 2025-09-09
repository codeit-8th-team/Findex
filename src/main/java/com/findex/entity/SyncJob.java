package com.findex.entity;

import com.findex.entity.base.BaseEntity;
import com.findex.enums.JobResult;
import com.findex.enums.JobType;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(name = "sync_job")
public class SyncJob extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "index_info_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)        // ERD: FK ON DELETE CASCADE
  private IndexInfo indexInfo;

  @Enumerated(EnumType.STRING)
  @Column(name = "job_type", nullable = false, length = 10)
  private JobType jobType;

  @Column(name = "target_date", nullable = false)   // ERD: NOT NULL
  private LocalDate targetDate;

  @Column(name = "worker", nullable = false, length = 100)
  private String worker;

  @Column(name = "job_time", nullable = false)
  private Instant jobTime;

  @Enumerated(EnumType.STRING)
  @Column(name = "result", nullable = false, length = 10)
  private JobResult result;

  public static SyncJob of(
      IndexInfo indexInfo,
      JobType jobType,
      LocalDate targetDate,
      String worker,
      Instant jobTime,
      JobResult result
  ) {
    SyncJob j = new SyncJob(); // 클래스 내부라 protected 접근 가능
    j.setIndexInfo(indexInfo);
    j.setJobType(jobType);
    j.setTargetDate(targetDate);
    j.setWorker(worker);
    j.setJobTime(jobTime);
    j.setResult(result);
    return j;
  }
}