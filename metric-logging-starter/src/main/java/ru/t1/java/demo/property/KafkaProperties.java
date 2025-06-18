package ru.t1.java.demo.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties(prefix = "t1.kafka")
public class KafkaProperties {

    private String server;
    private String metricTopic;

}
