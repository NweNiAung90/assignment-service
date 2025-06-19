package com.nna.assignment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nna.assignment.cntroller.ClientController;
import com.nna.assignment.dto.ClientRequestDto;
import com.nna.assignment.dto.ClientResponseDto;
import com.nna.assignment.exception.ExternalServiceException;
import com.nna.assignment.service.ClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.NestedServletException;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ClientControllerTest {

    @Mock
    private ClientService clientService;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private ClientController clientController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private ClientRequestDto clientRequestDto;
    private ClientResponseDto clientResponseDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(clientController).build();
        objectMapper = new ObjectMapper();
        clientRequestDto = new ClientRequestDto();
        clientRequestDto.setName("John Doe");
        clientRequestDto.setEmail("john.doe@example.com");

        clientResponseDto = new ClientResponseDto();
        clientResponseDto.setId(1L);
        clientResponseDto.setName("John Doe");
        clientResponseDto.setEmail("john.doe@example.com");
    }

    @Test
    void createClient_Success() throws Exception {
        when(clientService.create(any(ClientRequestDto.class))).thenReturn(clientResponseDto);

        mockMvc.perform(post("/clients/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clientRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));

        verify(clientService, times(1)).create(any(ClientRequestDto.class));
    }

    @Test
    void createClient_ServiceThrowsException() throws Exception {
        when(clientService.create(any(ClientRequestDto.class)))
                .thenThrow(new RuntimeException("Service error"));

        Exception exception = assertThrows(NestedServletException.class, () -> {
            mockMvc.perform(post("/clients/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(clientRequestDto)));
        });

        verify(clientService, times(1)).create(any(ClientRequestDto.class));
    }

    @Test
    void getClients_Success() throws Exception {
        List<ClientResponseDto> clientList = Arrays.asList(clientResponseDto);
        Page<ClientResponseDto> clientPage = new PageImpl<>(clientList, PageRequest.of(0, 10), 1);
        when(clientService.getClients(any(Pageable.class))).thenReturn(clientPage);

        mockMvc.perform(get("/clients")
                        .param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].name").value("John Doe"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(clientService, times(1)).getClients(any(Pageable.class));
    }

    @Test
    void getClients_DefaultPage() throws Exception {
        List<ClientResponseDto> clientList = Arrays.asList(clientResponseDto);
        Page<ClientResponseDto> clientPage = new PageImpl<>(clientList, PageRequest.of(0, 10), 1);
        when(clientService.getClients(any(Pageable.class))).thenReturn(clientPage);

        mockMvc.perform(get("/clients"))
                .andExpect(status().isOk());

        verify(clientService, times(1)).getClients(PageRequest.of(0, 10));
    }

    @Test
    void getClients_ServiceThrowsException() throws Exception {
        when(clientService.getClients(any(Pageable.class)))
                .thenThrow(new RuntimeException("Service error"));

        Exception exception = assertThrows(NestedServletException.class, () -> {
            mockMvc.perform(get("/clients"));
        });

        Throwable rootCause = exception.getCause();
        assertInstanceOf(RuntimeException.class, rootCause);
        assertEquals("Service error", rootCause.getMessage());
        verify(clientService, times(1)).getClients(any(Pageable.class));
    }

    @Test
    void getClient_Success() throws Exception {
        Long clientId = 1L;
        when(clientService.getClientById(clientId)).thenReturn(clientResponseDto);

        mockMvc.perform(get("/clients/{clientId}", clientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));

        verify(clientService, times(1)).getClientById(clientId);
    }

    @Test
    void getClient_ServiceThrowsException() throws Exception {
        Long clientId = 1L;
        when(clientService.getClientById(clientId))
                .thenThrow(new RuntimeException("Client not found"));

        Exception exception = assertThrows(NestedServletException.class, () -> {
            mockMvc.perform(get("/clients/{clientId}", clientId));
        });

        Throwable rootCause = exception.getCause();
        assertInstanceOf(RuntimeException.class, rootCause);
        verify(clientService, times(1)).getClientById(clientId);
    }

    @Test
    void modifyClient_Success() throws Exception {
        Long clientId = 1L;
        ClientResponseDto updatedClient = new ClientResponseDto();
        updatedClient.setId(clientId);
        updatedClient.setName("Updated Name");
        updatedClient.setEmail("updated@example.com");

        when(clientService.modifyClient(any(ClientRequestDto.class), eq(clientId)))
                .thenReturn(updatedClient);

        mockMvc.perform(put("/clients/{clientId}", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clientRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(clientId))
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.email").value("updated@example.com"));

        verify(clientService, times(1)).modifyClient(any(ClientRequestDto.class), eq(clientId));
    }

    @Test
    void callExternalApi_Success_MockMvc() throws Exception {
        String expectedResponse = "{\"id\": 1, \"title\": \"Test Post\", \"body\": \"Test content\"}";

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("https://jsonplaceholder.typicode.com/posts/1")).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(expectedResponse));

        mockMvc.perform(get("/clients/external"))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResponse));

        verify(webClient, times(1)).get();
        verify(requestHeadersUriSpec, times(1)).uri("https://jsonplaceholder.typicode.com/posts/1");
        verify(requestHeadersSpec, times(1)).retrieve();
        verify(responseSpec, times(1)).bodyToMono(String.class);
    }

    @Test
    void callExternalApi_WebClientRequestException_MockMvc() throws Exception {
        RuntimeException runtimeException = new RuntimeException("Connection timeout");


        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("https://jsonplaceholder.typicode.com/posts/1")).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.error(runtimeException));

        // When & Then
        Exception thrownException = assertThrows(NestedServletException.class, () -> {
            mockMvc.perform(get("/clients/external"));
        });

        // Verify the root cause is ExternalServiceException
        Throwable rootCause = thrownException.getCause();
        assertInstanceOf(ExternalServiceException.class, rootCause);
        assertEquals("Unexpected error occurred while calling external service", rootCause.getMessage());
        assertEquals(runtimeException, rootCause.getCause());

        verify(webClient, times(1)).get();
    }

    @Test
    void callExternalApi_WebClientResponseException_MockMvc() throws Exception {
        WebClientResponseException exception = WebClientResponseException.create(
                404,
                "Not Found",
                null,
                null,
                null
        );

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("https://jsonplaceholder.typicode.com/posts/1")).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.error(exception));

        Exception thrownException = assertThrows(NestedServletException.class, () -> {
            mockMvc.perform(get("/clients/external"));
        });

        Throwable rootCause = thrownException.getCause();
        assertInstanceOf(ExternalServiceException.class, rootCause);
        assertEquals("Unexpected error occurred while calling external service", rootCause.getMessage());
        assertEquals(exception, rootCause.getCause());

        verify(webClient, times(1)).get();
    }

}