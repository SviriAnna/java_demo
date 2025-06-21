package ru.t1.java.demo.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.t1.java.demo.dto.AccountDto;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.Client;

import java.util.UUID;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AccountMapper {

    @Mapping(source = "client.clientId", target = "clientId")
    AccountDto toDto(Account account);

    @Mapping(source = "id", target = "client.id") // маппит id DTO в id сущности (в суперклассе)
    @Mapping(source = "clientId", target = "client.clientId")
    Account toEntity(AccountDto dto);

    void updateEntityFromDto(AccountDto accountDto, @MappingTarget Account entity);

    default Client map(UUID id) {
        return id == null ? null : new Client(id);
    }
}
