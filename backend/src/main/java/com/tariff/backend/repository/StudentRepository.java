package com.tariff.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.tariff.backend.model.Student;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
  @Query("SELECT s FROM Student s WHERE s.name = ?1")
  Optional<Student> findStudentByName(String name);
}
