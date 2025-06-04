package ru.t1.java.demo.exception;

import java.math.BigDecimal;
import java.util.UUID;

public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException(UUID accountId, BigDecimal balance, BigDecimal attemptedWithdrawal) {
        super(String.format(
                "Недостаточно средств на счёте ID %s: текущий баланс %.2f, попытка списания %.2f",
                accountId, balance, attemptedWithdrawal
        ));
    }
}
