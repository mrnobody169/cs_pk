package com.pokertrack.mapper;

import com.pokertrack.model.AuditLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AuditLogMapper {
    void insert(AuditLog auditLog);
}
