package com.tariff.backend.dto;

public class UserDTO {
    public record UserDto(
    String email,
    String password
) {}
}
