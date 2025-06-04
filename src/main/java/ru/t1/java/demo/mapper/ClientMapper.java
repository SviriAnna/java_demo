package ru.t1.java.demo.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.t1.java.demo.dto.ClientDto;
import ru.t1.java.demo.model.Client;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ClientMapper {

    ClientDto toDto(Client client);

    Client toEntity(ClientDto clientDto);

    void updateEntityFromDto(ClientDto clientDto, @MappingTarget Client entity);

}
