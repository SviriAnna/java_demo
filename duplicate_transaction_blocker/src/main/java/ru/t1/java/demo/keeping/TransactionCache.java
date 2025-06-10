package ru.t1.java.demo.keeping;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.model.TransactionMessage;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@AllArgsConstructor
@Component
public class TransactionCache {

    private final Map<UUID, List<TransactionMessage>> transactionStore = new ConcurrentHashMap<>();
    private final DuplicateBlockerProperties duplicateBlockerProperties;

    public synchronized void addTransaction(TransactionMessage tx) {
        List<TransactionMessage> txList = transactionStore.computeIfAbsent(tx.getAccountId(), k -> new ArrayList<>());
        txList.add(tx);
        // Убираем транзакции старше окна
        LocalDateTime cutoff = tx.getTimestamp().minusSeconds(duplicateBlockerProperties.getWindowSeconds());
        txList.removeIf(t -> t.getTimestamp().isBefore(cutoff));
    }

    public synchronized List<TransactionMessage> getTransactionsInWindow(UUID accountId, LocalDateTime to) {
        List<TransactionMessage> txList = transactionStore.getOrDefault(accountId, new ArrayList<>());
        LocalDateTime cutoff = to.minusSeconds(duplicateBlockerProperties.getWindowSeconds());
        return txList.stream()
                .filter(t -> !t.getTimestamp().isBefore(cutoff) && !t.getTimestamp().isAfter(to))
                .collect(Collectors.toList());
    }
}
