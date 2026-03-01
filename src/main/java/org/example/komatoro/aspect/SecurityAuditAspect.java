package org.example.komatoro.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.nio.file.AccessDeniedException;

/**
 * Аспект для отслеживания удачных и не удачных авторизаций для методов с аннотацией @PreAuthorize
 */
@Aspect
@Component
@Slf4j
public class SecurityAuditAspect {

    @AfterReturning("@annotation(org.springframework.security.access.prepost.PreAuthorize)")
    public void securityAuditSuccess(JoinPoint joinPoint){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("User {} successfully accessed {}", auth.getName(), joinPoint.getSignature().toShortString());
    }

    @AfterThrowing(value = "@annotation(org.springframework.security.access.prepost.PreAuthorize)", throwing = "ex")
    public void securityAuditFail(JoinPoint joinPoint, AccessDeniedException ex){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.warn("User {} denied access to {}, exception: {}", auth.getName(),
                joinPoint.getSignature().toShortString(), ex.getMessage());
    }
}
