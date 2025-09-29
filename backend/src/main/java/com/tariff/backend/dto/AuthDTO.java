package com.tariff.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthDTO {
    // Using a Java Record for a concise DTO
     public record AuthRequest(
            @NotBlank(message = "Email is required.")
            @Email(message = "Please provide a valid email address.")
            String email,

            @NotBlank(message = "Password is required.")
            @Size(min = 6, message = "Password must be at least 6 characters long.")
            String password
    ) {}

    public record ChangePasswordRequest(
            @NotBlank(message = "Email is required.")
            @Email(message = "Please provide a valid email address.")
            String email,
            @NotBlank(message = "Current password is required.")
            String currentPassword,
            @NotBlank(message = "New password is required.")
            @Size(min = 6, message = "New password must be at least 6 characters long.")
            String newPassword
        ) {}
}
