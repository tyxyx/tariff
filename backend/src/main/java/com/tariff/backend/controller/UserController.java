package com.tariff.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



import com.tariff.backend.dto.UserRequestDTO;
import com.tariff.backend.model.User;
import com.tariff.backend.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping(path = "api/users")
public class UserController {
    private final UserService userService;

    // To-do JWT generation service 
    // private final JwtService jwtService; 

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public List<User> getAllUsers() {
        return this.userService.listUsers();
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @PostMapping("/register")
    public User registerUser(@Valid @RequestBody UserRequestDTO.AddUserDto addUserDto) {
       return userService.addUser(addUserDto);
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @PostMapping("/login")
    public User loginUser(@Valid @RequestBody UserRequestDTO.LoginDto loginDto) {
        return userService.loginUser(loginDto);
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @PutMapping("/change-password")
    public User updatePassword(@Valid @RequestBody UserRequestDTO.UpdatePasswordDto updatePasswordDto) {
        return userService.updatePassword(updatePasswordDto);
    }

}
