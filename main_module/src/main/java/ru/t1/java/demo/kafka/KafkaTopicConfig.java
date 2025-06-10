package ru.t1.java.demo.kafka;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic metricsTopic() {
        return TopicBuilder.name("t1_demo_metrics")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic transactionsTopic() {
        return TopicBuilder.name("t1_demo_transactions")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic transactionAcceptTopic() {
        return TopicBuilder.name("t1_demo_transaction_accept")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic transactionResultTopic() {
        return TopicBuilder.name("t1_demo_transaction_result")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public KafkaAdmin kafkaAdmin(@Value("${t1.kafka.bootstrap.server}") String bootstrapServers) {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(configs);
    }
}
