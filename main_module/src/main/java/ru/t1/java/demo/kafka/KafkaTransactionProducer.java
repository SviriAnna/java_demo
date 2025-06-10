package ru.t1.java.demo.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class KafkaTransactionProducer {

    private final KafkaTemplate<String, TransactionAcceptMessage> kafkaTemplate;

    private static final String TRANSACTION_ACCEPT_TOPIC = "t1_demo_transaction_accept";

    public void sendTransactionAccepted(TransactionAcceptMessage message) {
        kafkaTemplate.send(
                TRANSACTION_ACCEPT_TOPIC,
                message.getAccountId().toString(),
                message
        );
    }
}
