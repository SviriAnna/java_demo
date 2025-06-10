package ru.t1.java.demo.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.model.TimeLimitExceedLog;
import ru.t1.java.demo.repository.TimeLimitExceedLogRepository;
import ru.t1.java.demo.dto.TimeLimitExceedLogDto;

import java.nio.charset.StandardCharsets;

@Slf4j
@Aspect
@Component
@Order(3)
@RequiredArgsConstructor
public class MetricAspect {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final TimeLimitExceedLogRepository timeLimitExceedLogRepository;
    private final ObjectMapper objectMapper;

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

            if (executionTime > MAX_WANTED_TIME) {
                log.warn("Метод [{}] превысил лимит времени: {} > {}", methodSignature, executionTime, MAX_WANTED_TIME);

                TimeLimitExceedLogDto dto = new TimeLimitExceedLogDto(methodSignature, executionTime, MAX_WANTED_TIME);
                try {
                    String jsonMessage = objectMapper.writeValueAsString(dto);
                    ProducerRecord<String, String> record = new ProducerRecord<>("t1_demo_metrics", jsonMessage);
                    record.headers().add(new RecordHeader("errorType", "METRICS".getBytes(StandardCharsets.UTF_8)));

                    kafkaTemplate.send(record);
                    log.info("Сообщение METRICS отправлено в Kafka: {}", jsonMessage);
                } catch (Exception e) {
                    log.error("Не удалось отправить сообщение METRICS в Kafka. Ошибка: {}", e.getMessage(), e);
                    TimeLimitExceedLog logEntry = new TimeLimitExceedLog();
                    logEntry.setMethodSignature(methodSignature);
                    logEntry.setExecutionTime(executionTime);
                    timeLimitExceedLogRepository.save(logEntry);
                    log.info("Вместо отправки было сохранено в БД: {}", logEntry);
                }
            }
        }

        return result;
    }
}
