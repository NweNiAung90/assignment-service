package com.nna.assignment.service;

import com.nna.assignment.dto.ClientRequestDto;
import com.nna.assignment.dto.ClientResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClientService {
    ClientResponseDto create(ClientRequestDto client);

    Page<ClientResponseDto> getClients(Pageable pageable);

    ClientResponseDto getClientById(Long clientId);

    ClientResponseDto modifyClient(ClientRequestDto client, Long clientId);
}
