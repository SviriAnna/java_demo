package ru.t1.java.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.t1.java.demo.keeping.DuplicateBlockerProperties;
import ru.t1.java.demo.keeping.TransactionCache;
import ru.t1.java.demo.model.TransactionMessage;
import ru.t1.java.demo.model.TransactionResultMessage;
import ru.t1.java.demo.model.enums.TransactionStatus;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class TransactionProcessorService {

    private final DuplicateBlockerProperties properties;
    private final TransactionCache cache;

    public TransactionResultMessage process(TransactionMessage tx) {
        log.info("Поступила транзакция из топика t1_demo_transaction_accept. ClientId:{}, accountId{}, transactionId{}, Сумма транзакции: {}, Баланс счета: {}.", tx.getClientId(), tx.getAccountId(), tx.getTransactionId(), tx.getAmount(), tx.getBalance());

        if (tx.getAmount().compareTo(tx.getBalance()) > 0) {
            log.info("Недостаточно средств для проведения транзакции. Баланс счета: {}, сумма транзакции: {}.", tx.getBalance(), tx.getAmount());
            return buildResult(tx, TransactionStatus.REJECTED);
        }

        LocalDateTime now = tx.getTimestamp();
        cache.addTransaction(tx);

        List<TransactionMessage> windowTxs = cache.getTransactionsInWindow(tx.getAccountId(), now);
        int threshold = properties.getThreshold();
        int windowSize = windowTxs.size();

        if (windowSize <= threshold) {
            log.info("Транзакция с id {} одобрена", tx.getTransactionId());
            return buildResult(tx, TransactionStatus.ACCEPTED);
        } else if (windowSize <= threshold * 2) {
            log.info("Поступила подозрительная транзакция по счету {}, транзакция {} будет заблокирована.", tx.getAccountId(), tx.getTransactionId());
            return buildResult(tx, TransactionStatus.BLOCKED);
        } else {
            log.info("Транзакция c id {} отклонена по причине подозрительных транзакций с аккаунта {}.", tx.getTransactionId(), tx.getAccountId());
            return buildResult(tx, TransactionStatus.REJECTED);
        }
    }

    private TransactionResultMessage buildResult(TransactionMessage tx, TransactionStatus transactionStatus) {
        TransactionResultMessage result = new TransactionResultMessage();
        result.setTransactionId(tx.getTransactionId());
        result.setAccountId(tx.getAccountId());
        result.setTransactionStatus(transactionStatus);
        return result;
    }
}