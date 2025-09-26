package com.tariff.backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tariff.backend.repository.StudentRepository;
import com.tariff.backend.model.Student;

@Service
public class StudentService {
  private final StudentRepository studentRepository;

  @Autowired
  public StudentService(StudentRepository studentRepository) {
    this.studentRepository = studentRepository;
  }

  public List<Student> getStudent() {return this.studentRepository.findAll();}

  public void addStudent(Student student) {
    Optional<Student> optionalStudent = studentRepository.findStudentByName(student.getName());
    if (optionalStudent.isPresent()) {
      throw new IllegalStateException("already existed");
    }
    studentRepository.save(student);
  }

  public void deletStudent(Long id) {
    boolean find = studentRepository.existsById(id);
    if (!find) {
      throw new IllegalStateException("already existed");
    }
    studentRepository.deleteById(id);
  }
}
