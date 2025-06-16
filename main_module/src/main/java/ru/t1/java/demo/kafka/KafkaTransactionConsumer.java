package ru.t1.java.demo.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.dto.TransactionDto;
import ru.t1.java.demo.dto.TransactionResultDto;
import ru.t1.java.demo.service.TransactionService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class KafkaTransactionConsumer {

    private final TransactionService transactionService;

    @KafkaListener(id = "transaction-consumer",
            topics = {"t1_demo_transactions"},
            containerFactory = "transactionKafkaListenerContainerFactory")
    public void consume(@Payload List<TransactionDto> messageList,
                        Acknowledgment ack,
                        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {

        log.info("Transaction consumer: Получен батч из {} сообщений из топика {}", messageList.size(), topic);

        try {
            for (TransactionDto message : messageList) {
                transactionService.processTransaction(message);
            }
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Ошибка при обработке транзакции: ", e);
            // Можно решить, как поступать с ошибкой — либо вернуть, либо обработать
        }
    }

    @KafkaListener(
            id = "transaction-result-consumer",
            topics = "${t1.kafka.topic.transaction_result}",
            containerFactory = "transactionResultKafkaListenerContainerFactory"
    )
    public void consumeTransactionResults(@Payload List<TransactionResultDto> messageList,
                                          Acknowledgment ack,
                                          @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {

        log.info("TransactionResult consumer: Получен батч из {} сообщений из топика {}", messageList.size(), topic);

        for (TransactionResultDto message : messageList) {
            try {
                transactionService.handleTransactionResult(message);
            } catch (Exception e) {
                log.error("Ошибка при обработке результата транзакции: {}", message, e);
                return;
            }
        }
        ack.acknowledge();
    }
}