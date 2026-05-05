package org.example.placement_drive_management.repository;

import org.example.placement_drive_management.entity.Applications;
import org.example.placement_drive_management.entity.Student;
import org.example.placement_drive_management.entity.StudentProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StudentProfileRepository extends JpaRepository<StudentProfile,Long> {
    Optional<StudentProfile> findByStudentRollNo(String rollNo);
    Page<StudentProfile> findAll(Pageable pageable);
    boolean existsByStudent(Student student);
}
