package com.pokertrack.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pokertrack.mapper.AuditLogMapper;
import com.pokertrack.model.AuditLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);

    @Autowired
    private AuditLogMapper auditLogMapper;

    @Autowired
    private ObjectMapper objectMapper;

    public void log(Long groupId, String entityType, Long entityId,
            String action, Object before, Object after, Long actorUserId) {
        AuditLog auditLog = new AuditLog();
        auditLog.setGroupId(groupId);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setAction(action);
        auditLog.setActorUserId(actorUserId);
        auditLog.setBeforeJson(toJson(before));
        auditLog.setAfterJson(toJson(after));
        auditLogMapper.insert(auditLog);
    }

    private String toJson(Object obj) {
        if (obj == null)
            return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize audit payload", e);
            return "{}";
        }
    }
}
