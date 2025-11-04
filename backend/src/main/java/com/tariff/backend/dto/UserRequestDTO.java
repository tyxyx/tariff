package com.tariff.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class UserRequestDTO {
    public record AddUserDto(
            @NotBlank(message = "Email is required.") @Email(
                    message = "Please provide a valid email address."
            ) String email,
            @NotBlank(message = "Password is required.")
            String password
    ) {}

    public record LoginDto(
            @NotBlank(message = "Email is required.") @Email(
                    message = "Please provide a valid email address."
            ) String email,
            @NotBlank(message = "Password is required.") String password
    ) {}

    public record UpdatePasswordDto(
            @NotBlank(message = "Email is required.") @Email(
                    message = "Please provide a valid email address."
            ) String email,
            @NotBlank(message = "Current password is required.") String password,
            @NotBlank(message = "New password is required.")
            String newPassword
    ) {}

    public record DeleteUserDto(
            @NotBlank(message = "Email is required.")
            @Email(message = "Please provide a valid email address.")
            String email
    ) {}

    public record UpdateUserRoleDto(
            @NotBlank(message = "Email is required.") @Email(message = "Please provide a valid email address.")
            String email
    ) {}

    public record UpdateRoleResponse(
            boolean success,
            String message
    ) {}
}