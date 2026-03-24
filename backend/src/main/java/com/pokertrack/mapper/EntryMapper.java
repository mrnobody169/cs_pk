package com.pokertrack.mapper;

import com.pokertrack.dto.response.EntryView;
import com.pokertrack.model.Entry;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EntryMapper {
    Entry findById(@Param("id") Long id);

    List<EntryView> findViewsBySessionId(@Param("sessionId") Long sessionId);

    List<EntryView> findUnpaidBySessionId(@Param("sessionId") Long sessionId);

    Entry findBySessionAndMember(@Param("sessionId") Long sessionId, @Param("memberUserId") Long memberUserId);

    void insert(Entry entry);

    void update(Entry entry);

    void updateSettlement(Entry entry);

    void delete(@Param("id") Long id);
}
