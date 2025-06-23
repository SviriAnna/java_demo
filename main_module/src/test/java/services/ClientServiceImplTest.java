package services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import ru.t1.java.demo.dto.ClientDto;
import ru.t1.java.demo.exception.ClientNotFoundException;
import ru.t1.java.demo.mapper.ClientMapper;
import ru.t1.java.demo.model.Client;
import ru.t1.java.demo.repository.ClientRepository;
import ru.t1.java.demo.service.impl.ClientServiceImpl;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class ClientServiceImplTest {

    @InjectMocks
    private ClientServiceImpl clientService;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ClientMapper clientMapper;

    private UUID id;
    private UUID clientId;
    private Client client;
    private ClientDto clientDto;

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
        clientId = UUID.randomUUID();

        client = new Client();
        client.setId(id);
        client.setClientId(clientId);

        clientDto = new ClientDto();
        clientDto.setId(id);
        clientDto.setClientId(clientId);
    }

    @Test
    void getById_success() {
        when(clientRepository.findById(id)).thenReturn(Optional.of(client));
        when(clientMapper.toDto(client)).thenReturn(clientDto);

        ClientDto result = clientService.getById(id);

        assertEquals(clientDto, result);
        verify(clientRepository).findById(id);
    }

    @Test
    void getById_notFound_throwsException() {
        when(clientRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ClientNotFoundException.class, () -> clientService.getById(id));
    }

    @Test
    void getByClientId_success() {
        when(clientRepository.findByClientId(clientId)).thenReturn(Optional.of(client));
        when(clientMapper.toDto(client)).thenReturn(clientDto);

        ClientDto result = clientService.getByClientId(clientId);

        assertEquals(clientDto, result);
    }

    @Test
    void getByClientId_notFound_throwsException() {
        when(clientRepository.findByClientId(clientId)).thenReturn(Optional.empty());

        assertThrows(ClientNotFoundException.class, () -> clientService.getByClientId(clientId));
    }

    @Test
    void getAll_returnsList() {
        List<Client> clients = List.of(client);
        List<ClientDto> clientDtos = List.of(clientDto);

        when(clientRepository.findAll()).thenReturn(clients);
        when(clientMapper.toDto(client)).thenReturn(clientDto);

        List<ClientDto> result = clientService.getAll();

        assertEquals(clientDtos, result);
    }

    @Test
    void save_success() {
        when(clientMapper.toEntity(clientDto)).thenReturn(client);
        when(clientRepository.save(any(Client.class))).thenReturn(client);
        when(clientMapper.toDto(client)).thenReturn(clientDto);

        ClientDto result = clientService.save(clientDto);

        assertNotNull(result);
        verify(clientRepository).save(any(Client.class));
    }

    @Test
    void update_success() {
        when(clientRepository.findById(id)).thenReturn(Optional.of(client));
        doNothing().when(clientMapper).updateEntityFromDto(clientDto, client);
        when(clientRepository.save(client)).thenReturn(client);
        when(clientMapper.toDto(client)).thenReturn(clientDto);

        ClientDto result = clientService.update(clientDto);

        assertEquals(clientDto, result);
    }

    @Test
    void update_withNullId_throwsException() {
        clientDto.setId(null);
        assertThrows(IllegalArgumentException.class, () -> clientService.update(clientDto));
    }

    @Test
    void update_notFound_throwsException() {
        when(clientRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(ClientNotFoundException.class, () -> clientService.update(clientDto));
    }

    @Test
    void deleteById_success() {
        when(clientRepository.existsById(id)).thenReturn(true);

        clientService.deleteById(id);

        verify(clientRepository).deleteById(id);
    }

    @Test
    void deleteById_notFound_throwsException() {
        when(clientRepository.existsById(id)).thenReturn(false);

        assertThrows(ClientNotFoundException.class, () -> clientService.deleteById(id));
    }
}
