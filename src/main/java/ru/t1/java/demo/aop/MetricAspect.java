package ru.t1.java.demo.aop;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.model.TimeLimitExceedLog;
import ru.t1.java.demo.repository.TimeLimitExceedLogRepository;

@Slf4j
@Aspect
@Component
@Order(3)
@RequiredArgsConstructor
public class MetricAspect {

    private final TimeLimitExceedLogRepository timeLimitExceedLogRepository;
    @Value("${metric.method-time-limit}")
    private long MAX_WANTED_TIME;

    @Around("@annotation(ru.t1.java.demo.aop.annotation.Metric)")
    public Object logExecutionTimeAdvice(ProceedingJoinPoint joinPoint) throws Throwable {

        String methodSignature = joinPoint.getSignature().toLongString();

        long startTime = System.currentTimeMillis();
        Object result;
        try {
            result = joinPoint.proceed();
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;
            log.info("Execution time of " + methodSignature + " is " + executionTime + " ms");
            if(executionTime > MAX_WANTED_TIME) {
                TimeLimitExceedLog timeLimitExceedLog = new TimeLimitExceedLog();
                timeLimitExceedLog.setMethodSignature(methodSignature);
                timeLimitExceedLog.setExecutionTime(executionTime);

                timeLimitExceedLogRepository.save(timeLimitExceedLog);
            }
        }
        return result;
    }
}
