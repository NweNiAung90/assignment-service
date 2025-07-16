package com.nna.assignment.service.impl;

import com.nna.assignment.dto.ClientRequestDto;
import com.nna.assignment.dto.ClientResponseDto;
import com.nna.assignment.entity.Client;
import com.nna.assignment.exception.ClientNotFoundException;
import com.nna.assignment.exception.ClientValidationException;
import com.nna.assignment.repository.ClientRepository;
import com.nna.assignment.service.ClientService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.function.Consumer;


@Service
public class ClientServiceImpl implements ClientService {
    private static final Logger logger = LogManager.getLogger(ClientServiceImpl.class);

    @Autowired
    private ClientRepository repository;
    private ModelMapper modelMapper = new ModelMapper();

    @Override
    @Transactional
    public ClientResponseDto create(ClientRequestDto clientRequestDto) {
        try {
            validateClientRequest(clientRequestDto);

            if (repository.existsByEmail(clientRequestDto.getEmail())) {
                throw new ClientValidationException("Client with email " + clientRequestDto.getEmail() + " already exists");
            }

            Client client = modelMapper.map(clientRequestDto, Client.class);
            client.setCreatedAt(LocalDateTime.now());
            client.setUpdatedAt(LocalDateTime.now());
            logger.info("Client Information : {} ", client);
            client = repository.save(client);
            return modelMapper.map(client, ClientResponseDto.class);
        } catch (ClientValidationException e) {
            logger.error("Validation error creating client: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error creating client: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create client", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClientResponseDto> getClients(Pageable pageable) {
        try {
            return repository.findAll(pageable)
                    .map(client -> modelMapper.map(client, ClientResponseDto.class));
        } catch (Exception e) {
            logger.error("Error retrieving clients: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve clients", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ClientResponseDto getClientById(Long clientId) {
        try {
            if (clientId == null || clientId <= 0) {
                throw new ClientValidationException("Invalid client ID: " + clientId);
            }

            Client client = repository.findById(clientId)
                    .orElseThrow(() -> new ClientNotFoundException("Client not found with id: " + clientId));
            return modelMapper.map(client, ClientResponseDto.class);

        } catch (ClientNotFoundException | ClientValidationException ex) {
            logger.error("Error retrieving client {}: {}", clientId, ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            logger.error("Unexpected error retrieving client {}: {}", clientId, ex.getMessage(), ex);
            throw new RuntimeException("Failed to retrieve client", ex);
        }
    }

    @Override
    @Transactional
    public ClientResponseDto modifyClient(ClientRequestDto clientDto, Long clientId) {

        try {
            if (clientId == null || clientId <= 0) {
                throw new ClientValidationException("Invalid client ID: " + clientId);
            }

            validateClientRequest(clientDto);

            if (clientDto.getEmail() != null) {
                repository.findByEmailAndIdNot(clientDto.getEmail(), clientId)
                        .ifPresent(existingClient -> {
                            throw new ClientValidationException("Another client with email " + clientDto.getEmail() + " already exists");
                        });
            }


            Client clientInfo = repository.findById(clientId)
                    .map(client -> {
                        updateIfNotNull(client::setName, clientDto.getName());
                        updateIfNotNull(client::setEmail, clientDto.getEmail());
                        updateIfNotNull(client::setPhone, clientDto.getPhone());
                        client.setUpdatedAt(LocalDateTime.now());
                        return repository.save(client);
                    })
                    .orElseThrow(() -> new EntityNotFoundException("Client not found with id: " + clientId));
            return modelMapper.map(clientInfo, ClientResponseDto.class);
        } catch (ClientNotFoundException | ClientValidationException e) {
            logger.error("Error modifying client {}: {}", clientId, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error modifying client {}: {}", clientId, e.getMessage(), e);
            throw new RuntimeException("Failed to modify client", e);
        }
    }

    private <T> void updateIfNotNull(Consumer<T> setter, T value) {
        if (value != null) {
            setter.accept(value);
        }
    }

    private void validateClientRequest(ClientRequestDto clientDto) {
        if (clientDto == null) {
            throw new ClientValidationException("Client request cannot be null");
        }

        if (!isValidPhone(clientDto.getPhone())) {
            throw new ClientValidationException("Invalid phone format: " + clientDto.getPhone());
        }
    }

    private boolean isValidPhone(String phone) {
        return phone.matches("^[+]?[0-9]{10,15}$");
    }
}
