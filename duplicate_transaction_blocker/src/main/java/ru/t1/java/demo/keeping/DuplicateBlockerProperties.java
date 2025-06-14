package ru.t1.java.demo.keeping;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "transaction-checker")
public class DuplicateBlockerProperties {

    private int windowSeconds;
    private int threshold;

}
