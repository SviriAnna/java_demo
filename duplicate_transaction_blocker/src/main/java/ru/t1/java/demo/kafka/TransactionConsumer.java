package ru.t1.java.demo.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.model.TransactionMessage;
import ru.t1.java.demo.model.TransactionResultMessage;
import ru.t1.java.demo.service.TransactionProcessorService;

import java.util.List;

@RequiredArgsConstructor
@Component
public class TransactionConsumer {

    private final TransactionProcessorService processorService;
    private final KafkaTemplate<String, TransactionResultMessage> kafkaTemplate;

    @Value("${kafka.topics.transaction_result}")
    private String transactionResultTopic;

    @KafkaListener(
            topics = "${kafka.topics.transaction_accept}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(List<TransactionMessage> messages, Acknowledgment ack) {
        for (TransactionMessage message : messages) {
            TransactionResultMessage result = processorService.process(message);
            kafkaTemplate.send(transactionResultTopic, result.getAccountId().toString(), result);
        }
        ack.acknowledge();
    }
}