package test_utils;

import ru.t1.java.demo.dto.TransactionDto;
import ru.t1.java.demo.model.enums.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class TransactionTestUtils {

    public static TransactionDto createRequestedTransactionDto(UUID accountId) {
        TransactionDto dto = new TransactionDto();
        dto.setTransactionId(UUID.randomUUID());
        dto.setAccountId(accountId);
        dto.setAmount(new BigDecimal("300.00"));
        dto.setTransactionStatus(TransactionStatus.REQUESTED);
        dto.setTransactionTime(LocalDateTime.now());
        return dto;
    }
}
