package test_utils;

import ru.t1.java.demo.dto.ClientDto;
import ru.t1.java.demo.model.enums.ClientStatus;

import java.util.UUID;

public class ClientTestUtils {

    public static ClientDto createActiveClientDto() {
        ClientDto dto = new ClientDto();
        dto.setClientId(UUID.randomUUID());
        dto.setFirstName("Активный клиент");
        dto.setLastName("Активнович");
        dto.setMiddleName("Активнов");
        dto.setClientStatus(ClientStatus.ACTIVE);
        return dto;
    }

    public static ClientDto createBlockedClientDto() {
        ClientDto dto = new ClientDto();
        dto.setClientId(UUID.randomUUID());
        dto.setFirstName("Заблокированный клиент");
        dto.setLastName("Заблокировов");
        dto.setMiddleName("Заблокирович");
        dto.setClientStatus(ClientStatus.BLOCKED);
        return dto;
    }
}
