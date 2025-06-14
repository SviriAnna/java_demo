package ru.t1.java.demo.dto;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import ru.t1.java.demo.model.enums.TransactionStatus;

import java.util.UUID;

@JsonTypeName(value = "transactionsResults")
@Data
public class TransactionResultDto {

    private UUID transactionId;

    private UUID accountId;

    private TransactionStatus transactionStatus;
}
