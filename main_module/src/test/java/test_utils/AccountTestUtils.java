package test_utils;

import ru.t1.java.demo.dto.AccountDto;
import ru.t1.java.demo.model.enums.AccountStatus;
import ru.t1.java.demo.model.enums.AccountType;

import java.math.BigDecimal;
import java.util.UUID;

public class AccountTestUtils {

    public static AccountDto createOpenAccountDto(UUID clientId) {
        AccountDto dto = new AccountDto();
        dto.setAccountId(UUID.randomUUID());
        dto.setClientId(clientId);
        dto.setAccountType(AccountType.DEBIT);
        dto.setAccountStatus(AccountStatus.OPEN);
        dto.setBalance(new BigDecimal("100000.00"));
        dto.setFrozenAmount(new BigDecimal("0.00"));
        return dto;
    }

    public static AccountDto createBlockedAccountDto(UUID clientId) {
        AccountDto dto = new AccountDto();
        dto.setAccountId(UUID.randomUUID());
        dto.setClientId(clientId);
        dto.setAccountType(AccountType.CREDIT);
        dto.setAccountStatus(AccountStatus.BLOCKED);
        dto.setBalance(new BigDecimal("90000.00"));
        dto.setFrozenAmount(new BigDecimal("0.00"));
        return dto;
    }
}
