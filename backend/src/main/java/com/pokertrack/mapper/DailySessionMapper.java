package com.pokertrack.mapper;

import com.pokertrack.model.DailySession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface DailySessionMapper {
    DailySession findById(@Param("id") Long id);

    DailySession findByGroupAndDate(@Param("groupId") Long groupId, @Param("sessionDate") LocalDate sessionDate);

    List<DailySession> findByGroupIdAndDateRange(
            @Param("groupId") Long groupId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    void insert(DailySession session);

    void updateLock(@Param("id") Long id,
            @Param("dataLocked") Boolean dataLocked,
            @Param("lockedBy") Long lockedBy,
            @Param("lockedAt") java.time.OffsetDateTime lockedAt);
}
