package com.tariff.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.tariff.backend.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  @Query("SELECT s FROM User s WHERE s.username = ?1")
  Optional<User> findUserByUsername(String username);
}