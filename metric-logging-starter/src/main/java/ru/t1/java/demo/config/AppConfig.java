package ru.t1.java.demo.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import ru.t1.java.demo.property.KafkaProperties;
import ru.t1.java.demo.property.MetricProperties;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({KafkaProperties.class, MetricProperties.class})
@EnableAspectJAutoProxy
public class AppConfig {


}
