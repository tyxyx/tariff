package com.tariff.backend.controller;

import com.tariff.backend.dto.UserLoginDTO;
import com.tariff.backend.dto.UserRequestDTO;
import com.tariff.backend.model.User;
import com.tariff.backend.service.JwtService;
import com.tariff.backend.service.UserService;
import jakarta.validation.Valid;
import java.util.List;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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
    return userService.listUsers();
  }

  //   Return users with token for testing
  @GetMapping("/me")
  public ResponseEntity<User> authenticatedUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
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
    @Valid @RequestBody UserRequestDTO.LoginDto loginDto,
    HttpServletResponse response 
  ) {
    // Note that it takes in the record to auth user and returns seperate mutable DTO
    User authenticatedUser = userService.loginUser(loginDto);
    String jwtToken = jwtService.generateToken(authenticatedUser);

    // ✅ Set HttpOnly cookie
    Cookie cookie = new Cookie("auth_token", jwtToken);
    cookie.setHttpOnly(true);  // Prevents JavaScript access
    cookie.setSecure(true);    // Only sent over HTTPS (set false for local dev)
    cookie.setPath("/");       // Available for all paths
    cookie.setMaxAge(3 * 24 * 60 * 60); // 3 days in seconds
    cookie.setAttribute("SameSite", "Strict"); // CSRF protection
    
    response.addCookie(cookie);

    return ResponseEntity.ok(
      new UserLoginDTO(jwtToken, jwtService.getExpirationTime())
    );
  }

  @PutMapping("/change-password")
  public User updatePassword(@AuthenticationPrincipal UserDetails loggedInUser,
                             @Valid @RequestBody UserRequestDTO.UpdatePasswordDto updatePasswordDto) {
      String authenticatedEmail = loggedInUser.getUsername();
      return userService.updatePassword(authenticatedEmail, updatePasswordDto);
  }

  // @PutMapping("/change-email")
  // public User updatePassword(@Valid @RequestBody UserRequestDTO.UpdateEmailDto updateEmailDto) {
  //   return userService.updateEmail(updateEmailDto);
  // }

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

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {
      // Delete the cookie by setting maxAge to 0
      Cookie cookie = new Cookie("auth_token", null);
      cookie.setHttpOnly(true);
      cookie.setSecure(false);
      cookie.setPath("/");
      cookie.setMaxAge(0);  // Expire immediately
      
      response.addCookie(cookie);
      
      return ResponseEntity.ok("Logged out successfully");
    }
}
