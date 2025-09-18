package com.tariff.backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.tariff.backend.model.User;
import com.tariff.backend.repository.UserRepository;

@Service
public class UserService {
  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public List<User> listUsers() {
    return userRepository.findAll();
  }

  public void addUser(User user) {
    Optional<User> optionalUser =  userRepository.findUserByUsername(user.getUsername());
    if (optionalUser.isPresent()) {
      throw new IllegalStateException("User already exists");
    }
    userRepository.save(user);
  }

  public void deleteUser(long id) {
    boolean exist = userRepository.existsById(id);
    if (!exist) {
      throw new IllegalStateException("User not exists");
    }
    userRepository.deleteById(id);
  }
}
