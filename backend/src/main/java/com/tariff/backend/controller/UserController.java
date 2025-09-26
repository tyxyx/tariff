package com.tariff.backend.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tariff.backend.model.User;
import com.tariff.backend.service.UserService;
import com.tariff.backend.exception.BadRequestException;

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

  @PostMapping("add")
  public void postMethodName(@RequestBody User user) {
    this.userService.addUser(user);
  }

  @DeleteMapping("delete/{userId}")
  public void deleteUser(@PathVariable("userId") Long id) {
    userService.deleteUser(id);
  }

}
