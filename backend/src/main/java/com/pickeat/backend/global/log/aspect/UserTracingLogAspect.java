package com.pickeat.backend.global.log.aspect;

import com.pickeat.backend.global.log.LogWriter;
import com.pickeat.backend.global.log.model.bussiness.UserTracingLog;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class UserTracingLogAspect {

    @Around("@annotation(businessLogging)")
    public Object log(
            ProceedingJoinPoint joinPoint,
            UserTracingLogging userTracingLogging
    ) throws Throwable {
        String action = userTracingLogging.action();
        Long userId = extractUserId(joinPoint.getArgs());

        Object result = joinPoint.proceed();

        LogWriter.info(joinPoint.getTarget().getClass(), UserTracingLog.of(userId, action));
        return result;
    }

    //TODO: userId를 찾는 보다 정교한 매커니즘으로 변경 (2026-05-16, 토, 21:40)
    private Long extractUserId(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof Long userId) {
                return userId;
            }
        }
        return null;
    }
}
