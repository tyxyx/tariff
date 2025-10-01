package com.tariff.backend.service;

import java.util.List;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tariff.backend.dto.UserRequestDTO;
import com.tariff.backend.exception.user.UserAlreadyExistsException;
import com.tariff.backend.exception.user.UserNotFoundException;
import com.tariff.backend.exception.user.InvalidCredentialsException;
import com.tariff.backend.model.User;
import com.tariff.backend.repository.UserRepository;



@Service
public class UserService {
  private final UserRepository userRepository;
  private final BCryptPasswordEncoder passwordEncoder;

  public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public List<User> listUsers() {
    return userRepository.findAll();
  }

  private String hashPassword(String password) {
    return passwordEncoder.encode(password);
  }

  private void checkPasswordMatch(String rawPassword, String storedPassword) {
    if (!passwordEncoder.matches(rawPassword, storedPassword)) {
        throw new InvalidCredentialsException("Invalid email or password. Please try again.");
    }
  }


  private void checkPasswordStrength(String password) {
    if (!(password.length() >= 6 && password.matches(".*[A-Z].*") && password.matches(".*\\d.*"))) {
      throw new IllegalArgumentException("Password must be at least 8 characters long, contain an uppercase letter and a number.");
    }
  }

  public User addUser(UserRequestDTO.AddUserDto addUserDto) {
        userRepository.findByEmail(addUserDto.email()).ifPresent(user -> {
            throw new UserAlreadyExistsException("A user with this email already exists.");
        });

        checkPasswordStrength(addUserDto.password());
        User newUser = new User(addUserDto.email(), hashPassword(addUserDto.password()));
        return userRepository.save(newUser);
    }
  
  @Transactional(rollbackFor = Exception.class)
  public User loginUser(UserRequestDTO.LoginDto loginDto) {
    // Check if user already exists
    User user = this.userRepository.findByEmail(loginDto.email()).orElseThrow(() -> {
      return new UserNotFoundException("We couldn't find an account with that email. Please check your details.");}
    );

    checkPasswordMatch(loginDto.password(), user.getPassword());
    return user;
  }
  
  public User updatePassword(UserRequestDTO.UpdatePasswordDto updatePasswordDto) {
    // Check if user already exists
    User user = this.userRepository.findByEmail(updatePasswordDto.email()).orElseThrow(() -> {
      return new UserNotFoundException("User not found with email: " + updatePasswordDto.email());}
    );

    checkPasswordMatch(updatePasswordDto.password(), user.getPassword());
    checkPasswordStrength(updatePasswordDto.newPassword());

    user.setPassword(hashPassword(updatePasswordDto.newPassword()));
    return this.userRepository.save(user);
  }

} 