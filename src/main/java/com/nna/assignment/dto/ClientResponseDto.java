package com.nna.assignment.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ClientResponseDto {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
