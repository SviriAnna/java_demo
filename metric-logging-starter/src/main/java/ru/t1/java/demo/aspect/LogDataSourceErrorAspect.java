package ru.t1.java.demo.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.kafka.core.KafkaTemplate;
import ru.t1.java.demo.dto.DataSourceErrorLogDto;
import ru.t1.java.demo.model.DataSourceErrorLog;
import ru.t1.java.demo.repository.DataSourceErrorLogRepository;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

@Slf4j
@Aspect
@Order(2)
@RequiredArgsConstructor
public class LogDataSourceErrorAspect {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final DataSourceErrorLogRepository dataSourceErrorLogRepository;
    private final ObjectMapper objectMapper;

    @Pointcut("@annotation(ru.t1.java.demo.annotation.LogDataSourceError)")
    public void loggingDataSourcePointcut() {}

    @AfterThrowing(pointcut = "loggingDataSourcePointcut()", throwing = "ex")
    public void loggingDataSourceError(JoinPoint joinPoint, Exception ex) {
        String methodSignature = joinPoint.getSignature().toLongString();
        String stackTrace = getStackTrace(ex);
        String message = ex.getMessage();

        DataSourceErrorLogDto dto = new DataSourceErrorLogDto(methodSignature, message, stackTrace);

        try {
            String jsonMessage = objectMapper.writeValueAsString(dto);
            ProducerRecord<String, String> record = new ProducerRecord<>("t1_demo_metrics", jsonMessage);
            record.headers().add(new RecordHeader("errorType", "DATA_SOURCE".getBytes(StandardCharsets.UTF_8)));

//            throw new RuntimeException("Искусственная ошибка отправки в Kafka");
            kafkaTemplate.send(record);
            log.info("Сообщение DATA_SOURCE отправлено в Kafka: {}", jsonMessage);
        } catch (Exception kafkaEx) {
            log.error("Ошибка отправки DATA_SOURCE в Kafka: {}", kafkaEx.getMessage(), kafkaEx);

            DataSourceErrorLog entity = new DataSourceErrorLog();
            entity.setMethodSignature(methodSignature);
            entity.setMessage(message);
            entity.setStackTrace(stackTrace);
            dataSourceErrorLogRepository.save(entity);

            log.info("Вместо отправки сообщение записано в БД: {}", entity);
        }
    }

    private String getStackTrace(Exception ex) {
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}