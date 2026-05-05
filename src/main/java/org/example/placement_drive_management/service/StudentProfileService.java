package org.example.placement_drive_management.service;

import lombok.AllArgsConstructor;
import org.example.placement_drive_management.dto.*;
import org.example.placement_drive_management.entity.Applications;
import org.example.placement_drive_management.entity.Student;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public interface StudentProfileService {
    String createStudentProfile(StudentProfileDto studentProfileDto,String rollNumberInContext);
    String updateStudentProfile(StudentProfileDto studentProfileDto,String rollNumberInContext);
    StudentProfileDto getStudentProfile(String rollNumberInContext);
    PageResponse<ApplicationsDto> getAllApplicationsForStudent(String rollNumberInContext, int  page, int size);
    List<ApplicationRoundDto> getAllApplicationRoundsForStudentAndDriveId(String driveId,String rollNumberInContext);
    String applyDrive(String driveId, String rollNoInContext);
    PageResponse<ApplicationsDto> getAllEligibleApplications(String rollNumberInContext, int  page, int size);
    String uploadResume(MultipartFile file, String rollNo);
    ResponseEntity<byte[]> streamResume(String email);
    long countEligibleDrives(String rollNo);
    long countApplicationsByStatus(String rollNo, String status);
}
