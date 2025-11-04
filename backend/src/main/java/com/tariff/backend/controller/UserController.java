package com.tariff.backend.controller;

import com.tariff.backend.dto.UserLoginDTO;
import com.tariff.backend.dto.UserRequestDTO;
import com.tariff.backend.model.User;
import com.tariff.backend.service.JwtService;
import com.tariff.backend.service.UserService;
import jakarta.validation.Valid;
import java.util.List;

import org.hibernate.sql.Update;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "api/users")
public class UserController {

  private final JwtService jwtService;
  private final UserService userService;

  public UserController(UserService userService, JwtService jwtService) {
    this.userService = userService;
    this.jwtService = jwtService;
  }

  @GetMapping("/")
  public List<User> getAllUsers() {
    return this.userService.listUsers();
  }

  //   Return users with token for testing
  @GetMapping("/me")
  public ResponseEntity<User> authenticatedUser() {
    Authentication authentication = SecurityContextHolder.getContext()
      .getAuthentication();

    User currentUser = (User) authentication.getPrincipal();

    return ResponseEntity.ok(currentUser);
  }

  @PostMapping("/register")
  public User registerUser(
    @Valid @RequestBody UserRequestDTO.AddUserDto addUserDto
  ) {
    return userService.addUser(addUserDto);
  }

  @PostMapping("/login")
  public ResponseEntity<UserLoginDTO> loginUser(
    @Valid @RequestBody UserRequestDTO.LoginDto loginDto
  ) {
    // Note that it takes in the record to auth user and returns seperate mutable DTO
    User authenticatedUser = userService.loginUser(loginDto);
    String jwtToken = jwtService.generateToken(authenticatedUser);

    return ResponseEntity.ok(
      new UserLoginDTO(jwtToken, jwtService.getExpirationTime())
    );
  }

  @PutMapping("/change-password")
  public User updatePassword(
    @Valid @RequestBody UserRequestDTO.UpdatePasswordDto updatePasswordDto
  ) {
    return userService.updatePassword(updatePasswordDto);
  }

    @DeleteMapping("/")
    public User deleteUser(@Valid @RequestBody UserRequestDTO.DeleteUserDto deleteUserDto) {
        return userService.deleteUser(deleteUserDto);
    }

    @PutMapping("/upgrade-role")
    public User upgradeRole(@Valid @RequestBody UserRequestDTO.UpdateUserRoleDto updateUserRoleDto) {
      return userService.upgradeRole(updateUserRoleDto);
    }

    @PutMapping("/downgrade-role")
    public User downgradeRole(@Valid @RequestBody UserRequestDTO.UpdateUserRoleDto updateUserRoleDto) {
        return userService.downgradeRole(updateUserRoleDto);
    }
}
