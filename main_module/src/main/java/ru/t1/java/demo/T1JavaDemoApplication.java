package ru.t1.java.demo;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ru.t1.java.demo.aspect.LogDataSourceErrorAspect;
import ru.t1.java.demo.cache.CacheProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@Slf4j
@EnableConfigurationProperties(CacheProperties.class)
@EnableTransactionManagement
@EnableAsync
public class T1JavaDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(T1JavaDemoApplication.class, args);
    }

    @Bean
    public ApplicationRunner logBeans(ApplicationContext ctx) {
        return args -> {
            System.out.println("Beans of type LogDataSourceErrorAspect:");
            String[] beanNames = ctx.getBeanNamesForType(LogDataSourceErrorAspect.class);
            for (String name : beanNames) {
                System.out.println(" - " + name);
            }
        };
    }
}
