package org.curriculumai.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);
    private final AuditRepository auditRepository;

    public AuditService(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordEvent(String runId, String actor, String action, String entityType, String entityId, String safeSummary, String status) {
        String eventId = "audit-" + UUID.randomUUID();
        String correlationId = "corr-" + UUID.randomUUID().toString().substring(0, 8);

        AuditEvent event = new AuditEvent(eventId, runId, actor, action, entityType, entityId, safeSummary, status, correlationId);
        auditRepository.save(event);

        log.info("[AUDIT] action={} actor={} entityType={} entityId={} status={} corrId={}",
                action, actor, entityType, entityId, status, correlationId);
    }
}

