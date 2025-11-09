package com.tariff.backend.service;

import com.tariff.backend.dto.UserRequestDTO;
import com.tariff.backend.exception.InvalidCredentialsException;
import com.tariff.backend.exception.UserAlreadyExistsException;
import com.tariff.backend.model.User;
import com.tariff.backend.repository.UserRepository;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
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

  @Value("${security.password.min-length:8}") // Default to 8 if property is missing
  private int minPwdLength;

  public UserService(
    UserRepository userRepository,
    BCryptPasswordEncoder passwordEncoder,
    AuthenticationManager authenticationManager
  ) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.authenticationManager = authenticationManager;
  }

  // Helper Functions
  public List<User> listUsers() {
    return userRepository.findAll();
  }

  private String hashPassword(String password) {
    return passwordEncoder.encode(password);
  }

  private void checkPasswordMatch(String rawPassword, String storedPassword) {
    if (!passwordEncoder.matches(rawPassword, storedPassword)) {
      throw new InvalidCredentialsException(
        "Invalid password. Please try again."
      );
    }
  }

  private User findUserByEmailOrThrow(String email) {
      // Check if user already exists
      return userRepository.findByEmail(email)
              .orElseThrow(() -> new UsernameNotFoundException(
                      "User not found with email: " + email
              ));
  }

  // Service Layer stuffs
  // todo make password minimum length dynamic - OK, can change at application.properties
  // allow admin to change this implementation - this required to be store in db, sounds like a pain
  private void checkPasswordStrength(String password) {
    if (
      !(password.length() >= minPwdLength &&
        password.matches(".*[A-Z].*") &&
        password.matches(".*\\d.*"))
    ) {
      throw new IllegalArgumentException(
        String.format("Password must be at least %d characters long, contain an uppercase letter and a number.", minPwdLength)
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
            null
    );
    return userRepository.save(newUser);
  }

  @Transactional(rollbackFor = Exception.class)
  public User loginUser(UserRequestDTO.LoginDto loginDto) {
    User user = findUserByEmailOrThrow(loginDto.email());

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

  // todo fix weird bug
  // if jwt token is incorrect (like idk someone stole another user jwt and tried to login as a admin),
  // it will return 200 without updating the password in db, idkkkkkkkk
  public User updatePassword(UserRequestDTO.UpdatePasswordDto updatePasswordDto) {
    User user = findUserByEmailOrThrow(updatePasswordDto.email());

    checkPasswordMatch(updatePasswordDto.password(), user.getPassword());
    checkPasswordStrength(updatePasswordDto.newPassword());

    user.setPassword(hashPassword(updatePasswordDto.newPassword()));
    return userRepository.save(user);
  }

  // public User updateEmail(UserRequestDTO.UpdateEmailDto updateEmailDto) {
  //   User user = findUserByEmailOrThrow(updateEmailDto.email());

  //   if (updateEmailDto.email().equals(updateEmailDto.newEmail())) {
  //     throw new InvalidCredentialsException("New email must be different from the current email");
  //   };

  //   user.setEmail(updateEmailDto.newEmail());
  //   return userRepository.save(user);
  // }

  public User deleteUser(UserRequestDTO.DeleteUserDto deleteUserDto) {
      User user = findUserByEmailOrThrow(deleteUserDto.email());
      userRepository.delete(user);
      return user;
  }

  public User upgradeRole(UserRequestDTO.UpdateUserRoleDto updateUserRoleDto) {
      User user = findUserByEmailOrThrow(updateUserRoleDto.email());
      user.upgradeRole();
      return userRepository.save(user);
  }

    public User downgradeRole(UserRequestDTO.UpdateUserRoleDto updateUserRoleDto) {
        User user = findUserByEmailOrThrow(updateUserRoleDto.email());
        user.downgradeRole();
        return userRepository.save(user);
    }
}
