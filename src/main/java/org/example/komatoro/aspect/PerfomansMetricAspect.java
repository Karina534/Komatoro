package org.example.komatoro.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * Аспект для логирования медленных запросов в контроллерах с аннотацией @PerfomansMetric
 */
@Aspect
@Component
@Slf4j
public class PerfomansMetricAspect {

    @Around("@annotation(org.example.komatoro.annotation.PerfomansMetric)")
    public Object needPerfomansMethricPointcut(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        try {
            return joinPoint.proceed();
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            if (duration > 5000){
                log.warn("Slow query endpoint {} took {} ms", joinPoint.getSignature().toShortString(), duration);
            }
        }
    }
}
