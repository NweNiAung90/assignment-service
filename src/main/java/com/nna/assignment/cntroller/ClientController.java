package com.nna.assignment.cntroller;

import com.nna.assignment.dto.ClientRequestDto;
import com.nna.assignment.dto.ClientResponseDto;
import com.nna.assignment.exception.ExternalServiceException;
import com.nna.assignment.service.ClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import javax.validation.Valid;

@RestController
@RequestMapping("/clients")
public class ClientController {
    private static final Logger logger = LogManager.getLogger(ClientController.class);

    private final ClientService clientService;
    private final WebClient webClient;

    @Autowired
    public ClientController(ClientService clientService, WebClient webClient) {
        this.clientService = clientService;
        this.webClient = webClient;
    }

    @Operation(summary = "saving one client")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OK",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad Request",
                    content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not Found",
                    content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "503", description = "Service Unavailable",
                    content = @Content)})
    @PostMapping("/create")
    public ResponseEntity<ClientResponseDto> create(@Valid @RequestBody ClientRequestDto clientDto) {
        try {
            ClientResponseDto createdClient = clientService.create(clientDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdClient);
        } catch (Exception e) {
            logger.error("Error creating client: {}", e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "Retrieving all clients")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OK",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "503", description = "Service Unavailable",
                    content = @Content)})
    @GetMapping
    public ResponseEntity<Page<ClientResponseDto>> getClients(@RequestParam(defaultValue = "0") int page) {
        try {
            Pageable pageable = PageRequest.of(page, 10);
            Page<ClientResponseDto> clients = clientService.getClients(pageable);
            logger.info("Retrieved {} clients", clients.getContent().size());
            return ResponseEntity.ok(clients);
        } catch (Exception e) {
            logger.error("Error retrieving clients: {}", e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "retrieving one client by ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OK",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad Request",
                    content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not Found",
                    content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "503", description = "Service Unavailable",
                    content = @Content)})
    @GetMapping("/{clientId}")
    public ResponseEntity<ClientResponseDto> getClient(@PathVariable(value = "clientId") Long clientId) {
        try {

            ClientResponseDto client = clientService.getClientById(clientId);
            return new ResponseEntity<>(client, HttpStatus.OK);

        } catch (Exception e) {
            logger.error("Error retrieving client {}: {}", clientId, e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "modifying one client")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OK",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad Request",
                    content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not Found",
                    content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "503", description = "Service Unavailable",
                    content = @Content)})
    @PutMapping("/{clientId}")
    public ResponseEntity<ClientResponseDto> modifyClient(@Valid @RequestBody ClientRequestDto request, @PathVariable(value = "clientId") Long clientId) {
        try {
            ClientResponseDto updatedClient = clientService.modifyClient(request, clientId);
            return ResponseEntity.status(HttpStatus.OK).body(updatedClient);
        } catch (Exception e) {
            logger.error("Error modifying client {}: {}", clientId, e.getMessage());
            throw e;
        }
    }

    @GetMapping("/external")
    public ResponseEntity<String> callExternalApi() {
        try {
            String response = webClient
                    .get()
                    .uri("https://jsonplaceholder.typicode.com/posts/1")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Unexpected error during external API call: {}", e.getMessage());
            throw new ExternalServiceException("Unexpected error occurred while calling external service", e);
        }
    }

}
