package ru.t1.java.demo.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.t1.java.demo.dto.TransactionDto;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.Transaction;

import java.util.UUID;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TransactionMapper {

    @Mapping(source = "account.id", target = "accountId")
    TransactionDto toDto(Transaction transaction);

    @Mapping(source = "accountId", target = "account")
    Transaction toEntity(TransactionDto transactionDto);

    void updateEntityFromDto(TransactionDto transactionDto, @MappingTarget Transaction entity);

    default Account map(UUID accountId) {
        if (accountId == null) return null;
        Account account = new Account();
        account.setId(accountId);
        return account;
    }

}
