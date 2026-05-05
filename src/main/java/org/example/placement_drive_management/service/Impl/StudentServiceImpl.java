package org.example.placement_drive_management.service.Impl;

import lombok.AllArgsConstructor;
import org.example.placement_drive_management.dto.PageResponse;
import org.example.placement_drive_management.dto.StudentDto;
import org.example.placement_drive_management.dto.StudentResponseDto;
import org.example.placement_drive_management.entity.Applications;
import org.example.placement_drive_management.entity.Student;
import org.example.placement_drive_management.entity.StudentProfile;
import org.example.placement_drive_management.exceptions.ResourceNotFoundException;
import org.example.placement_drive_management.mappers.PageMapper;
import org.example.placement_drive_management.mappers.StudentMapper;
import org.example.placement_drive_management.mappers.StudentResponseMapper;
import org.example.placement_drive_management.repository.StudentProfileRepository;
import org.example.placement_drive_management.repository.StudentRepository;
import org.example.placement_drive_management.service.StudentService;
import org.hibernate.annotations.SecondaryRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class StudentServiceImpl implements StudentService {
private StudentRepository studentRepository;
@Autowired
PasswordEncoder passwordEncoder;

    @Override
    public StudentResponseDto createStudent(StudentDto studentDto) {
        Student student = StudentMapper.maptoStudent(studentDto);
        student.setPassword(passwordEncoder.encode(studentDto.getPassword()));
        Student newStudent= studentRepository.save(student);
        return StudentResponseMapper.maptoStudentResponseDto(newStudent);
    }

    @Override
    public Student getStudentByRollNo(String rollNo) {
        return studentRepository.findByRollNo(rollNo).orElseThrow(()->new ResourceNotFoundException("Student with Roll No :"+rollNo+"not found"));
    }

    @Override
    public PageResponse<StudentResponseDto> getAllStudents(int page, int size) {
        Pageable pageable = PageRequest.of(page,size, Sort.by("rollNo").ascending());
        Page<StudentResponseDto> students = studentRepository.findAll(pageable).map(student ->  StudentResponseMapper.maptoStudentResponseDto(student));
        return PageMapper.mapToPageResponse(students);
    }



}
