package com.latoscana.branchnode.common.audit;

import com.latoscana.branchnode.identity.domain.User;
import com.latoscana.branchnode.identity.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import jakarta.persistence.Id;
import java.lang.reflect.Field;
import java.util.Optional;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    // Define pointcut to match any save or delete operation in repositories EXCEPT AuditLogRepository and UserRepository (to lower recursion/spam)
    @Pointcut("execution(* com.latoscana.branchnode..*Repository.save*(..)) && !execution(* com.latoscana.branchnode.common.audit.AuditLogRepository.*(..)) && !execution(* com.latoscana.branchnode.sync..*Repository.*(..))")
    public void saveMethods() {}

    @Pointcut("execution(* com.latoscana.branchnode..*Repository.delete*(..)) && !execution(* com.latoscana.branchnode.common.audit.AuditLogRepository.*(..)) && !execution(* com.latoscana.branchnode.sync..*Repository.*(..))")
    public void deleteMethods() {}

    @AfterReturning(pointcut = "saveMethods()", returning = "result")
    public void auditSave(JoinPoint joinPoint, Object result) {
        if (result == null) return;
        logAction("UPSERT", result);
    }

    @AfterReturning("deleteMethods()")
    public void auditDelete(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args != null && args.length > 0) {
            logAction("DELETE", args[0]); // Best-effort: arg might be ID or Entity directly
        }
    }

    private void logAction(String action, Object entityOrId) {
        try {
            Long currentUserId = getCurrentUserId();
            String entityName = entityOrId.getClass().getSimpleName();
            String entityIdStr = extractEntityId(entityOrId);

            AuditLog log = new AuditLog(currentUserId, action, entityName, entityIdStr);
            auditLogRepository.save(log);
        } catch (Exception e) {
            // Failsafe: Audit errors should not crash the main transaction directly in this simple aspect approach
            e.printStackTrace();
        }
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            String username = auth.getName();
            Optional<User> uOpt = userRepository.findByUsername(username);
            if (uOpt.isPresent()) {
                return uOpt.get().getId();
            }
        }
        return null; // System action or unauthenticated
    }

    private String extractEntityId(Object entity) {
        if (entity instanceof Long || entity instanceof Integer || entity instanceof String) {
            return entity.toString(); // For deleteById
        }
        try {
            for (Field field : entity.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(Id.class)) {
                    field.setAccessible(true);
                    Object idVal = field.get(entity);
                    return idVal != null ? idVal.toString() : null;
                }
            }
        } catch (Exception e) {
            return "unknown";
        }
        return "unknown";
    }
}
