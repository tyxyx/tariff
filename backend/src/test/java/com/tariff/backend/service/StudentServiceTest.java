package com.tariff.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tariff.backend.model.Student;
import com.tariff.backend.repository.StudentRepository;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    private StudentService studentService;

    @BeforeEach
    void setUp() {
        studentService = new StudentService(studentRepository);
    }

    @Test
    void getStudentShouldReturnAllStudents() {
        List<Student> students = List.of(new Student("Alice"), new Student("Bob"));
        when(studentRepository.findAll()).thenReturn(students);

        List<Student> result = studentService.getStudent();

        assertThat(result).isEqualTo(students);
        verify(studentRepository).findAll();
    }

    @Test
    void addStudentShouldPersistWhenNameIsUnique() {
        Student student = new Student("Charlie");
        when(studentRepository.findStudentByName("Charlie")).thenReturn(Optional.empty());

        studentService.addStudent(student);

        verify(studentRepository).save(student);
    }

    @Test
    void addStudentShouldThrowWhenNameAlreadyExists() {
        Student student = new Student("Charlie");
        when(studentRepository.findStudentByName("Charlie")).thenReturn(Optional.of(student));

        assertThatThrownBy(() -> studentService.addStudent(student))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("already existed");
        verify(studentRepository, never()).save(student);
    }

    @Test
    void deleteStudentShouldDeleteWhenIdExists() {
        when(studentRepository.existsById(1L)).thenReturn(true);

        studentService.deletStudent(1L);

        verify(studentRepository).deleteById(1L);
    }

    @Test
    void deleteStudentShouldThrowWhenIdMissing() {
        when(studentRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> studentService.deletStudent(99L))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("already existed");
    }
}
