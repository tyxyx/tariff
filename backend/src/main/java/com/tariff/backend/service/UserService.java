package com.tariff.backend.service;

import com.tariff.backend.dto.UserRequestDTO;
import com.tariff.backend.exception.InvalidCredentialsException;
import com.tariff.backend.exception.UserAlreadyExistsException;
import com.tariff.backend.model.ERole;
import com.tariff.backend.model.User;
import com.tariff.backend.repository.UserRepository;
import java.util.List;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

  private final UserRepository userRepository;
  private final BCryptPasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;

  public UserService(
    UserRepository userRepository,
    BCryptPasswordEncoder passwordEncoder,
    AuthenticationManager authenticationManager
  ) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.authenticationManager = authenticationManager;
  }

  public List<User> listUsers() {
    return userRepository.findAll();
  }

  private String hashPassword(String password) {
    return passwordEncoder.encode(password);
  }

  private void checkPasswordMatch(String rawPassword, String storedPassword) {
    if (!passwordEncoder.matches(rawPassword, storedPassword)) {
      throw new InvalidCredentialsException(
        "Invalid email or password. Please try again."
      );
    }
  }

  // To-do make password minimum length dynamic, allow admin to change this implementation
  private void checkPasswordStrength(String password) {
    if (
      !(password.length() >= 6 &&
        password.matches(".*[A-Z].*") &&
        password.matches(".*\\d.*"))
    ) {
      throw new IllegalArgumentException(
        "Password must be at least 8 characters long, contain an uppercase letter and a number."
      );
    }
  }

  public User addUser(UserRequestDTO.AddUserDto addUserDto) {
    userRepository
      .findByEmail(addUserDto.email())
      .ifPresent(user -> {
        throw new UserAlreadyExistsException(
          "A user with this email already exists."
        );
      });

    checkPasswordStrength(addUserDto.password());
    User newUser = new User(
      addUserDto.email(),
      hashPassword(addUserDto.password()),
      ERole.ROLE_USER
    );
    return userRepository.save(newUser);
  }

  @Transactional(rollbackFor = Exception.class)
  public User loginUser(UserRequestDTO.LoginDto loginDto) {
    // Check if user already exists
    User user =
      this.userRepository.findByEmail(loginDto.email()).orElseThrow(() -> {
          return new UsernameNotFoundException(
            "We couldn't find an account with that email. Please check your details."
          );
        });

    // Decode password to check if ok
    checkPasswordMatch(loginDto.password(), user.getPassword());

    // Authenticate Manager
    authenticationManager.authenticate(
      new UsernamePasswordAuthenticationToken(
        loginDto.email(),
        loginDto.password()
      )
    );

    return user;
  }

  public User updatePassword(
    UserRequestDTO.UpdatePasswordDto updatePasswordDto
  ) {
    // Check if user already exists
    User user =
      this.userRepository.findByEmail(updatePasswordDto.email()).orElseThrow(
          () -> {
            return new UsernameNotFoundException(
              "User not found with email: " + updatePasswordDto.email()
            );
          }
        );

    checkPasswordMatch(updatePasswordDto.password(), user.getPassword());
    checkPasswordStrength(updatePasswordDto.newPassword());

    user.setPassword(hashPassword(updatePasswordDto.newPassword()));
    return this.userRepository.save(user);
  }
}
