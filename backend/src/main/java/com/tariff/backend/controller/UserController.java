package com.tariff.backend.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tariff.backend.model.User;
import com.tariff.backend.service.UserService;
import com.tariff.backend.exception.BadRequestException;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping(path = "api/users")
public class UserController {
  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }


  @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<String> handleResourceNotFound(BadRequestException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

  @GetMapping("/")
  public List<User> getAllUsers() {
    return this.userService.listUsers();
  }

  // @DeleteMapping("delete/{userId}")
  // public void deleteUser(@PathVariable("userId") Long id) {
  //   userService.deleteUser(id);
  // }
  @CrossOrigin(origins = "http://localhost:3000")
  @PostMapping("/register")
   public ResponseEntity<User> registerUser(@RequestBody Map<String, String> payload) {
       String email = payload.get("email");
       String password = payload.get("password");
       // Basic validation
       if (email == null || password == null || password.length() < 6) {
           return ResponseEntity.badRequest().body(Map.of("message", "Email and password are required and password must be at least 6 characters."));
       }
       try {
           User user =  userService.registerNewUser(email, password);
           return ResponseEntity.created(null).body(user);
       } catch (IllegalStateException e) {
           return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
       } catch (Exception e) {
           return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", e + "An error occurred during registration."));
       }
   }

  @CrossOrigin(origins = "http://localhost:3000")
  @PostMapping("/login")
  public ResponseEntity<Map<String, String>> loginUser(@RequestBody Map<String, String> payload) {
       String email = payload.get("email");
       String password = payload.get("password");

       if (email == null || password == null) {
           return ResponseEntity.badRequest().body(Map.of("message", "Email and password are required."));
       }

       if (userService.loginUser(email, password)) {
           // to-do: Session/JWT
           return ResponseEntity.ok(Map.of("message", "Login successful!"));
       } else {
           return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid email or password."));
       }
   }

}
