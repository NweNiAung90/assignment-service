package com.nna.assignment;

import com.nna.assignment.dto.ClientRequestDto;
import com.nna.assignment.dto.ClientResponseDto;
import com.nna.assignment.entity.Client;
import com.nna.assignment.exception.ClientNotFoundException;
import com.nna.assignment.exception.ClientValidationException;
import com.nna.assignment.repository.ClientRepository;
import com.nna.assignment.service.impl.ClientServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceImplTest {

    @Mock
    private ClientRepository repository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ClientServiceImpl clientService;

    private ClientRequestDto clientRequestDto;
    private Client client;
    private ClientResponseDto clientResponseDto;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(clientService, "modelMapper", modelMapper);

        clientRequestDto = new ClientRequestDto();
        clientRequestDto.setName("John Doe");
        clientRequestDto.setEmail("john.doe@example.com");
        clientRequestDto.setPhone("+1234567890");

        client = new Client();
        client.setId(1L);
        client.setName("John Doe");
        client.setEmail("john.doe@example.com");
        client.setPhone("+1234567890");
        client.setCreatedAt(LocalDateTime.now());
        client.setUpdatedAt(LocalDateTime.now());

        clientResponseDto = new ClientResponseDto();
        clientResponseDto.setId(1L);
        clientResponseDto.setName("John Doe");
        clientResponseDto.setEmail("john.doe@example.com");
        clientResponseDto.setPhone("+1234567890");
    }

    @Test
    void create_WithValidClient_ShouldReturnClientResponseDto() {
        when(repository.existsByEmail(clientRequestDto.getEmail())).thenReturn(false);
        when(modelMapper.map(clientRequestDto, Client.class)).thenReturn(client);
        when(repository.save(any(Client.class))).thenReturn(client);
        when(modelMapper.map(client, ClientResponseDto.class)).thenReturn(clientResponseDto);

        ClientResponseDto result = clientService.create(clientRequestDto);

        assertNotNull(result);
        assertEquals(clientResponseDto.getId(), result.getId());
        assertEquals(clientResponseDto.getName(), result.getName());
        assertEquals(clientResponseDto.getEmail(), result.getEmail());
        assertEquals(clientResponseDto.getPhone(), result.getPhone());

        verify(repository).existsByEmail(clientRequestDto.getEmail());
        verify(repository).save(any(Client.class));
        verify(modelMapper).map(clientRequestDto, Client.class);
        verify(modelMapper).map(client, ClientResponseDto.class);
    }

    @Test
    void create_WithNullClientRequest_ShouldThrowClientValidationException() {
        ClientValidationException exception = assertThrows(
                ClientValidationException.class,
                () -> clientService.create(null)
        );

        assertEquals("Client request cannot be null", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void create_WithInvalidEmail_ShouldThrowClientValidationException() {
        clientRequestDto.setEmail("invalid-email");

        ClientValidationException exception = assertThrows(
                ClientValidationException.class,
                () -> clientService.create(clientRequestDto)
        );

        assertEquals("Invalid email format: invalid-email", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void create_WithInvalidPhone_ShouldThrowClientValidationException() {
        clientRequestDto.setPhone("123");

        ClientValidationException exception = assertThrows(
                ClientValidationException.class,
                () -> clientService.create(clientRequestDto)
        );

        assertEquals("Invalid phone format: 123", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void create_WithExistingEmail_ShouldThrowClientValidationException() {
        when(repository.existsByEmail(clientRequestDto.getEmail())).thenReturn(true);

        ClientValidationException exception = assertThrows(
                ClientValidationException.class,
                () -> clientService.create(clientRequestDto)
        );

        assertEquals("Client with email john.doe@example.com already exists", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void create_WithRepositoryException_ShouldThrowRuntimeException() {
        when(repository.existsByEmail(clientRequestDto.getEmail())).thenReturn(false);
        when(modelMapper.map(clientRequestDto, Client.class)).thenReturn(client);
        when(repository.save(any(Client.class))).thenThrow(new RuntimeException("Database error"));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> clientService.create(clientRequestDto)
        );

        assertEquals("Failed to create client", exception.getMessage());
    }

    @Test
    void getClients_WithValidPageable_ShouldReturnPageOfClientResponseDto() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Client> clients = Arrays.asList(client);
        Page<Client> clientPage = new PageImpl<>(clients, pageable, 1);

        when(repository.findAll(pageable)).thenReturn(clientPage);
        when(modelMapper.map(client, ClientResponseDto.class)).thenReturn(clientResponseDto);

        Page<ClientResponseDto> result = clientService.getClients(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        verify(repository).findAll(pageable);
    }

    @Test
    void getClients_WithRepositoryException_ShouldThrowRuntimeException() {
        Pageable pageable = PageRequest.of(0, 10);
        when(repository.findAll(pageable)).thenThrow(new RuntimeException("Database error"));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> clientService.getClients(pageable)
        );

        assertEquals("Failed to retrieve clients", exception.getMessage());
    }

    @Test
    void getClientById_WithValidId_ShouldReturnClientResponseDto() {
        Long clientId = 1L;
        when(repository.findById(clientId)).thenReturn(Optional.of(client));
        when(modelMapper.map(client, ClientResponseDto.class)).thenReturn(clientResponseDto);

        ClientResponseDto result = clientService.getClientById(clientId);

        assertNotNull(result);
        assertEquals(clientResponseDto.getId(), result.getId());
        assertEquals(clientResponseDto.getName(), result.getName());
        verify(repository).findById(clientId);
        verify(modelMapper).map(client, ClientResponseDto.class);
    }

    @Test
    void getClientById_WithNullId_ShouldThrowClientValidationException() {
        ClientValidationException exception = assertThrows(
                ClientValidationException.class,
                () -> clientService.getClientById(null)
        );

        assertEquals("Invalid client ID: null", exception.getMessage());
        verify(repository, never()).findById(any());
    }

    @Test
    void getClientById_WithZeroId_ShouldThrowClientValidationException() {
        ClientValidationException exception = assertThrows(
                ClientValidationException.class,
                () -> clientService.getClientById(0L)
        );

        assertEquals("Invalid client ID: 0", exception.getMessage());
        verify(repository, never()).findById(any());
    }

    @Test
    void getClientById_WithNegativeId_ShouldThrowClientValidationException() {
        ClientValidationException exception = assertThrows(
                ClientValidationException.class,
                () -> clientService.getClientById(-1L)
        );

        assertEquals("Invalid client ID: -1", exception.getMessage());
        verify(repository, never()).findById(any());
    }

    @Test
    void getClientById_WithNonExistentId_ShouldThrowClientNotFoundException() {
        Long clientId = 999L;
        when(repository.findById(clientId)).thenReturn(Optional.empty());

        ClientNotFoundException exception = assertThrows(
                ClientNotFoundException.class,
                () -> clientService.getClientById(clientId)
        );

        assertEquals("Client not found with id: 999", exception.getMessage());
        verify(repository).findById(clientId);
    }

    @Test
    void getClientById_WithRepositoryException_ShouldThrowRuntimeException() {
        Long clientId = 1L;
        when(repository.findById(clientId)).thenThrow(new RuntimeException("Database error"));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> clientService.getClientById(clientId)
        );

        assertEquals("Failed to retrieve client", exception.getMessage());
    }

    @Test
    void modifyClient_WithValidData_ShouldReturnUpdatedClientResponseDto() {
        Long clientId = 1L;
        ClientRequestDto updateDto = new ClientRequestDto();
        updateDto.setName("Jane Doe");
        updateDto.setEmail("jane.doe@example.com");
        updateDto.setPhone("+9876543210");

        Client updatedClient = new Client();
        updatedClient.setId(1L);
        updatedClient.setName("Jane Doe");
        updatedClient.setEmail("jane.doe@example.com");
        updatedClient.setPhone("+9876543210");

        ClientResponseDto updatedResponseDto = new ClientResponseDto();
        updatedResponseDto.setId(1L);
        updatedResponseDto.setName("Jane Doe");
        updatedResponseDto.setEmail("jane.doe@example.com");
        updatedResponseDto.setPhone("+9876543210");

        when(repository.findByEmailAndIdNot(updateDto.getEmail(), clientId))
                .thenReturn(Optional.empty());
        when(repository.findById(clientId)).thenReturn(Optional.of(client));
        when(repository.save(any(Client.class))).thenReturn(updatedClient);
        when(modelMapper.map(updatedClient, ClientResponseDto.class)).thenReturn(updatedResponseDto);

        ClientResponseDto result = clientService.modifyClient(updateDto, clientId);

        assertNotNull(result);
        assertEquals(updatedResponseDto.getName(), result.getName());
        assertEquals(updatedResponseDto.getEmail(), result.getEmail());
        assertEquals(updatedResponseDto.getPhone(), result.getPhone());
        verify(repository).findById(clientId);
        verify(repository).save(any(Client.class));
    }

    @Test
    void modifyClient_WithPartialUpdate_ShouldUpdateOnlyProvidedFields() {
        Long clientId = 1L;
        ClientRequestDto updateDto = new ClientRequestDto();
        updateDto.setName("Jane Doe");

        when(repository.findById(clientId)).thenReturn(Optional.of(client));
        when(repository.save(any(Client.class))).thenReturn(client);
        when(modelMapper.map(client, ClientResponseDto.class)).thenReturn(clientResponseDto);

        ClientResponseDto result = clientService.modifyClient(updateDto, clientId);

        assertNotNull(result);
        verify(repository).findById(clientId);
        verify(repository).save(any(Client.class));
        verify(repository, never()).findByEmailAndIdNot(any(), any());
    }

    @Test
    void modifyClient_WithNullId_ShouldThrowClientValidationException() {
        ClientValidationException exception = assertThrows(
                ClientValidationException.class,
                () -> clientService.modifyClient(clientRequestDto, null)
        );

        assertEquals("Invalid client ID: null", exception.getMessage());
        verify(repository, never()).findById(any());
    }

    @Test
    void modifyClient_WithInvalidId_ShouldThrowClientValidationException() {
        ClientValidationException exception = assertThrows(
                ClientValidationException.class,
                () -> clientService.modifyClient(clientRequestDto, 0L)
        );

        assertEquals("Invalid client ID: 0", exception.getMessage());
        verify(repository, never()).findById(any());
    }

    @Test
    void modifyClient_WithNullClientDto_ShouldThrowClientValidationException() {
        ClientValidationException exception = assertThrows(
                ClientValidationException.class,
                () -> clientService.modifyClient(null, 1L)
        );

        assertEquals("Client request cannot be null", exception.getMessage());
        verify(repository, never()).findById(any());
    }

    @Test
    void modifyClient_WithInvalidEmail_ShouldThrowClientValidationException() {
        clientRequestDto.setEmail("invalid-email");

        ClientValidationException exception = assertThrows(
                ClientValidationException.class,
                () -> clientService.modifyClient(clientRequestDto, 1L)
        );

        assertEquals("Invalid email format: invalid-email", exception.getMessage());
        verify(repository, never()).findById(any());
    }

    @Test
    void modifyClient_WithInvalidPhone_ShouldThrowClientValidationException() {
        clientRequestDto.setPhone("123");

        ClientValidationException exception = assertThrows(
                ClientValidationException.class,
                () -> clientService.modifyClient(clientRequestDto, 1L)
        );

        assertEquals("Invalid phone format: 123", exception.getMessage());
        verify(repository, never()).findById(any());
    }

    @Test
    void modifyClient_WithEmailAlreadyExists_ShouldThrowClientValidationException() {
        Long clientId = 1L;
        Client existingClient = new Client();
        existingClient.setId(2L);
        existingClient.setEmail(clientRequestDto.getEmail());

        when(repository.findByEmailAndIdNot(clientRequestDto.getEmail(), clientId))
                .thenReturn(Optional.of(existingClient));

        ClientValidationException exception = assertThrows(
                ClientValidationException.class,
                () -> clientService.modifyClient(clientRequestDto, clientId)
        );

        assertEquals("Another client with email john.doe@example.com already exists", exception.getMessage());
        verify(repository, never()).findById(any());
    }

    @Test
    void modifyClient_WithRepositoryException_ShouldThrowRuntimeException() {
        Long clientId = 1L;
        when(repository.findByEmailAndIdNot(clientRequestDto.getEmail(), clientId))
                .thenReturn(Optional.empty());
        when(repository.findById(clientId)).thenThrow(new RuntimeException("Database error"));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> clientService.modifyClient(clientRequestDto, clientId)
        );

        assertEquals("Failed to modify client", exception.getMessage());
    }

    @Test
    void validateClientRequest_WithValidPhoneFormats_ShouldPass() {
        String[] validPhones = {
                "+1234567890",
                "1234567890",
                "+123456789012345",
                "1234567890123456"
        };

        for (int i = 0; i < validPhones.length - 1; i++) {
            clientRequestDto.setPhone(validPhones[i]);
            clientRequestDto.setEmail("test" + i + "@example.com");

            when(repository.existsByEmail(anyString())).thenReturn(false);
            when(modelMapper.map(any(ClientRequestDto.class), eq(Client.class))).thenReturn(client);
            when(repository.save(any(Client.class))).thenReturn(client);
            when(modelMapper.map(any(Client.class), eq(ClientResponseDto.class))).thenReturn(clientResponseDto);

            assertDoesNotThrow(() -> clientService.create(clientRequestDto));
        }
    }

    @Test
    void validateClientRequest_WithInvalidPhoneFormats_ShouldThrowException() {
        String[] invalidPhones = {
                "123",           // too short
                "12345678901234567890", // too long
                "abcdefghij",    // non-numeric
                "+abc1234567890" // mixed characters
        };

        for (String invalidPhone : invalidPhones) {
            clientRequestDto.setPhone(invalidPhone);

            ClientValidationException exception = assertThrows(
                    ClientValidationException.class,
                    () -> clientService.create(clientRequestDto)
            );

            assertTrue(exception.getMessage().contains("Invalid phone format"));
        }
    }
}