package com.findex.mapper;


import com.findex.dto.sync.SyncJobDto;
import com.findex.entity.SyncJob;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SyncJobMapper {
  @Mapping(target = "indexInfoId",
      expression = "java(job.getIndexInfo()!=null ? job.getIndexInfo().getId() : null)")
  SyncJobDto toDto(SyncJob job);
}
