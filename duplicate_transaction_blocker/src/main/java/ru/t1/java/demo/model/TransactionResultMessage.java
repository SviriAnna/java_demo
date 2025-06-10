package ru.t1.java.demo.model;

import lombok.Data;
import ru.t1.java.demo.model.enums.TransactionStatus;

import java.util.UUID;

@Data
public class TransactionResultMessage {

    private UUID transactionId;
    private UUID accountId;
    private TransactionStatus transactionStatus;

}
