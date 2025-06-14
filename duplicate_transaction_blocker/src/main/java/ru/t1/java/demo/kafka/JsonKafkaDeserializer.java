package ru.t1.java.demo.kafka;

import org.apache.kafka.common.header.Headers;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.nio.charset.StandardCharsets;

public class JsonKafkaDeserializer<T> extends JsonDeserializer<T> {

    @Override
    public T deserialize(String topic, byte[] data) {
        System.out.println("Deserialized TransactionMessage: " + super.deserialize(topic, data));

        if (data == null) {
            return null;
        }
        try {
            System.out.println("Trying to deserialize: " + new String(data, StandardCharsets.UTF_8));
            return super.deserialize(topic, data);
        } catch (Exception e) {
            throw new RuntimeException("Deserialization failed", e);
        }
    }

    @Override
    public T deserialize(String topic, Headers headers, byte[] data) {
        System.out.println("Deserialized TransactionMessage: " + super.deserialize(topic, data));

        if (data == null) {
            return null;
        }
        try {
            System.out.println("Trying to deserialize with headers: " + new String(data, StandardCharsets.UTF_8));
            return super.deserialize(topic, headers, data);
        } catch (Exception e) {
            throw new RuntimeException("Deserialization failed", e);
        }
    }
}
