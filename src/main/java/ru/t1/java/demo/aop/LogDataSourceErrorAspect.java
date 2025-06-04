package ru.t1.java.demo.aop;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.model.DataSourceErrorLog;
import ru.t1.java.demo.service.DataSourceErrorLogService;

import java.io.PrintWriter;
import java.io.StringWriter;

@Slf4j
@Aspect
@Component
@AllArgsConstructor
public class LogDataSourceErrorAspect {

    private final DataSourceErrorLogService dataSourceErrorLogService;

    @Pointcut("@annotation(ru.t1.java.demo.aop.annotation.LogDataSourceError)")
    public void loggingDataSourcePointcut() {
    }

    @AfterThrowing(
            pointcut = "loggingDataSourcePointcut()",
            throwing = "ex")
    public void loggingDataSourceError(JoinPoint joinPoint, Exception ex) {
        try {
            String methodSignature = joinPoint.getSignature().toLongString();
            String exceptionType = ex.getClass().getSimpleName();
            String errorMessage = ex.getMessage();

            log.info("Attempting to save error log for exception [{}] from method: {}", exceptionType, methodSignature);

            DataSourceErrorLog dataSourceErrorLog = new DataSourceErrorLog();
            dataSourceErrorLog.setStackTrace(formatStackTrace(ex));
            dataSourceErrorLog.setMessage(errorMessage);
            dataSourceErrorLog.setMethodSignature(methodSignature);

            dataSourceErrorLogService.save(dataSourceErrorLog);

            log.info("Successfully saved error log for exception [{}] from method: {}", exceptionType, methodSignature);

        } catch (Exception e) {
            log.error("Failed to log exception to DataSourceErrorLog: {}", e.getMessage(), e);
        }
    }

    private String formatStackTrace(Exception ex) {
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
