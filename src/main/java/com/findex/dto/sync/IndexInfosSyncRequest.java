package com.findex.dto.sync;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record IndexInfosSyncRequest(
    @NotEmpty(message = "indexInfoIds는 비어 있을 수 없습니다.")
    List<Long> indexInfoIds
) {}