package ru.t1.java.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DuplicateTransactionBlockerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DuplicateTransactionBlockerApplication.class, args);
    }
}