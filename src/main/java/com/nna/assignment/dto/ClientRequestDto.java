package com.nna.assignment.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ClientRequestDto {
    @NotNull
    @NotBlank(message = "Name is required")
    private String name;
    @NotNull
    @Email
    @NotBlank(message = "Email is required")
    private String email;
    @NotNull
    @NotBlank(message = "Phone is required")
    private String phone;
}
