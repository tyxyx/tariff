package com.tariff.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tariff.backend.model.Student;
import com.tariff.backend.service.StudentService;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping(path = "api/students")
public class StudentController {
  private final StudentService studentService;

  @Autowired
  public StudentController(StudentService studentService) {
    this.studentService = studentService;
  }

  @GetMapping("allStudents")
  public List<Student> getAllStudents() {
    return this.studentService.getStudent();
  }

  @PostMapping("addStudent")
  public void postMethodName(@RequestBody Student student) {
    this.studentService.addStudent(student);
  }

  @DeleteMapping("delete/{studentId}")
  public void deleteStudent(@PathVariable("studentId") Long id) {
    studentService.deletStudent(id);
  }

}
