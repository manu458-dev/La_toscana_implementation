package com.latoscana.branchnode.sync.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.latoscana.branchnode.sync.domain.SyncQueue;
import com.latoscana.branchnode.sync.domain.SyncQueueRepository;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.Id;
import java.lang.reflect.Field;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Aspect
@Component
@RequiredArgsConstructor
public class SyncPostTransactionHook {

    private final SyncQueueRepository syncQueueRepository;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Pointcut("execution(* com.latoscana.branchnode..*Repository.save*(..)) && !execution(* com.latoscana.branchnode.sync..*Repository.*(..)) && !execution(* com.latoscana.branchnode.common.audit..*(..))")
    public void saveMethods() {}

    @Pointcut("execution(* com.latoscana.branchnode..*Repository.delete*(..)) && !execution(* com.latoscana.branchnode.sync..*Repository.*(..)) && !execution(* com.latoscana.branchnode.common.audit..*(..))")
    public void deleteMethods() {}

    @AfterReturning(pointcut = "saveMethods()", returning = "result")
    @Transactional(propagation = Propagation.MANDATORY) // It must run inside the same transaction
    public void captureSaveForSync(JoinPoint joinPoint, Object result) {
        if (result == null) return;
        enqueueSyncMessage("UPSERT", result);
    }

    @AfterReturning("deleteMethods()")
    @Transactional(propagation = Propagation.MANDATORY)
    public void captureDeleteForSync(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args != null && args.length > 0) {
            enqueueSyncMessage("DELETE", args[0]);
        }
    }

    private void enqueueSyncMessage(String action, Object entityOrId) {
        try {
            String entityName = entityOrId.getClass().getSimpleName();
            String entityIdStr = extractEntityId(entityOrId);
            String payload = objectMapper.writeValueAsString(entityOrId); // Serialize entity state

            SyncQueue message = new SyncQueue(entityName, entityIdStr, action, payload);
            syncQueueRepository.save(message); // Saves within the same transaction to guarantee ATOMICITY (Outbox pattern)
        } catch (Exception e) {
            // Log it but do not crash the original transaction if serialization fails during POC
            System.err.println("Outbox Serialization Error: " + e.getMessage());
        }
    }

    private String extractEntityId(Object entity) {
        if (entity instanceof Long || entity instanceof Integer || entity instanceof String) {
            return entity.toString(); 
        }
        try {
            for (Field field : entity.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(Id.class)) {
                    field.setAccessible(true);
                    Object idVal = field.get(entity);
                    return idVal != null ? idVal.toString() : null;
                }
            }
        } catch (Exception e) {}
        return "unknown";
    }
}
