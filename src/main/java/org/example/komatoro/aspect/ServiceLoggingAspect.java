package org.example.komatoro.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Аспект для логирования выполнения методов во всех сервисах
 */

@Aspect
@Component
@Slf4j
@Order()
public class ServiceLoggingAspect {

    @Pointcut("within(org.example.komatoro.service.*) &&" +
            "within(@org.springframework.stereotype.Service *)")
    public void loggingServiceMethodPointcut() {}

    @Before("loggingServiceMethodPointcut()")
    public void loggingBeforeServiceMethod(JoinPoint joinPoint){
        String methodName = joinPoint.getSignature().toShortString();
        log.info("Starts method: {}", methodName);
    }

    @AfterReturning("loggingServiceMethodPointcut()")
    public void loggingAfterReturningServiceMethod(JoinPoint joinPoint){
        String methodName = joinPoint.getSignature().toShortString();
        log.info("Successfully ended method: {}", methodName);
    }

    @AfterThrowing("loggingServiceMethodPointcut()")
    public void loggingAfterThrowingServiceMethod(JoinPoint joinPoint){
        String methodName = joinPoint.getSignature().toShortString();
        log.info("Unsuccessfully ended method: {}", methodName);
    }
}
