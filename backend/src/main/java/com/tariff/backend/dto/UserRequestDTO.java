package com.tariff.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserRequestDTO {
         public record AddUserDto(
                @NotBlank(message = "Email is required.")
                @Email(message = "Please provide a valid email address.")
                String email,

                @NotBlank(message = "Password is required.")
                @Size(min = 6, message = "Password must be at least 6 characters long.")
                String password
        ) {}

        public record LoginDto(
            @NotBlank(message = "Email is required.")
            @Email(message = "Please provide a valid email address.")
            String email,

            @NotBlank(message = "Password is required.")
            String password
        ) {}

        public record UpdatePasswordDto(
            @NotBlank(message = "Email is required.")
            @Email(message = "Please provide a valid email address.")
            String email,
            @NotBlank(message = "Current password is required.")
            String password,
            @NotBlank(message = "New password is required.")
            @Size(min = 6, message = "New password must be at least 6 characters long.")
            String newPassword
        ) {}

        public record ChangePasswordResponse(
                boolean success,
                String message
        ) {}

}
