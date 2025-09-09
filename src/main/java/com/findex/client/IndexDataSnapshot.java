package com.findex.client;

import java.time.LocalDate;

public record IndexDataSnapshot(
    Integer employedItemsCount,
    LocalDate basePointInTime,
    Integer baseIndex,
    LocalDate baseDate // basDt (연동 대상 날짜)
) {}
