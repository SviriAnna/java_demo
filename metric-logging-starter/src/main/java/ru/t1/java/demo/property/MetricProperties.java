package ru.t1.java.demo.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "metric")
public class MetricProperties {

    private long methodTimeLimit;

}
