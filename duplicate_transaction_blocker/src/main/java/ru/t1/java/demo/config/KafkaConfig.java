package ru.t1.java.demo.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;
import ru.t1.java.demo.kafka.JsonKafkaDeserializer;
import ru.t1.java.demo.kafka.JsonKafkaSerializer;
import ru.t1.java.demo.model.TransactionMessage;
import ru.t1.java.demo.model.TransactionResultMessage;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Slf4j
public class KafkaConfig {

    @Value("${t1.kafka.server}")
    private String servers;

    @Value("${kafka.consumer.group-id}")
    private String consumerGroupId;

    @Value("${t1.kafka.session.timeout.ms:45000}")
    private String sessionTimeout;

    @Value("${t1.kafka.max.partition.fetch.bytes:300000}")
    private String maxPartitionFetchBytes;

    @Value("${t1.kafka.max.poll.records:1}")
    private String maxPollRecords;

    @Value("${t1.kafka.max.poll.interval.ms:300000}")
    private String maxPollIntervalsMs;

    @Value("${kafka.consumer.heartbeat.interval}")
    private String heartbeatInterval;

    // ==== CONSUMER CONFIGURATION ====

    @Bean
    public ConsumerFactory<String, TransactionMessage> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonKafkaDeserializer.class);
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "ru.t1.java.demo.model.TransactionMessage");
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, sessionTimeout);
        props.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, maxPartitionFetchBytes);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, maxPollIntervalsMs);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, Boolean.FALSE);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, heartbeatInterval);

        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, TransactionMessage> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, TransactionMessage> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setBatchListener(true);
        factory.setConcurrency(1);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.setCommonErrorHandler(errorHandler());
        return factory;
    }

    private CommonErrorHandler errorHandler() {
        DefaultErrorHandler handler = new DefaultErrorHandler(new FixedBackOff(1000L, 3L));
        handler.addNotRetryableExceptions(IllegalStateException.class);
        handler.setRetryListeners((record, ex, deliveryAttempt) -> {
            log.error("RetryListeners message = {}, offset = {}, deliveryAttempt = {}",
                    ex.getMessage(), record.offset(), deliveryAttempt);
        });
        return handler;
    }

    // ==== PRODUCER CONFIGURATION ====

    @Bean("duplicateTransactionProducerFactory")
    public ProducerFactory<String, TransactionResultMessage> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonKafkaSerializer.class);

        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean("duplicateTransactionKafkaTemplate")
    public KafkaTemplate<String, TransactionResultMessage> kafkaTemplate(
            @Qualifier("duplicateTransactionProducerFactory") ProducerFactory<String, TransactionResultMessage> pf) {
        return new KafkaTemplate<>(pf);
    }
}
