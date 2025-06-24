package ru.t1.java.demo.autoconfig;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import ru.t1.java.demo.aspect.LogDataSourceErrorAspect;
import ru.t1.java.demo.aspect.MetricAspect;
import ru.t1.java.demo.property.KafkaProperties;
import ru.t1.java.demo.property.MetricProperties;
import ru.t1.java.demo.repository.DataSourceErrorLogRepository;
import ru.t1.java.demo.repository.TimeLimitExceedLogRepository;

@AutoConfiguration
@EnableConfigurationProperties({KafkaProperties.class, MetricProperties.class})
@ConditionalOnProperty(prefix = "metric.logging", name = "enabled", havingValue = "true", matchIfMissing = true)
public class MetricLoggingAutoConfiguration {

    @Bean
    @ConditionalOnBean({KafkaTemplate.class, TimeLimitExceedLogRepository.class, ObjectMapper.class})
    public MetricAspect metricAspect(KafkaTemplate<String, String> kafkaTemplate,
                                     TimeLimitExceedLogRepository metricRepository,
                                     ObjectMapper objectMapper) {
        return new MetricAspect(kafkaTemplate, metricRepository, objectMapper);
    }

    @Bean
    @ConditionalOnBean({KafkaTemplate.class, DataSourceErrorLogRepository.class, ObjectMapper.class})
    public LogDataSourceErrorAspect logDataSourceErrorAspect(KafkaTemplate<String, String> kafkaTemplate,
                                                             DataSourceErrorLogRepository errorRepository,
                                                             ObjectMapper objectMapper) {
        return new LogDataSourceErrorAspect(kafkaTemplate, errorRepository, objectMapper);
    }
}
