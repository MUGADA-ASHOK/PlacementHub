package org.example.placement_drive_management.service;
import org.springframework.http.ResponseEntity;
import org.example.placement_drive_management.dto.*;
import org.example.placement_drive_management.dto.auth.ApiResponse;
import org.example.placement_drive_management.entity.Company;
import org.example.placement_drive_management.entity.Drive;
import org.example.placement_drive_management.entity.Student;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
@Service
public interface AdminService {
    List<StudentResponseDto> getAllStudents();
    StudentProfileDto getStudentProfileByRollNo(String rollNo);
    String createDrive(DriveDto driveDto);
    String createEligibility(EligibilityDto eligibilityDto);
    String updateEligibility(String driveId,EligibilityDto eligibilityDto);
    List<StudentProfileDto> getAllProfiles();
    String publishDrivesToEligibleStudents(String driveId);
    List<DriveDto> getAllDrives(String companyId);
    List<CompanyDto> getAllCompanies();
    List<ApplicationsDto> getAllApplicationsForaStudent(String rollNo);
    String closeDrive(String driveId);

    List<DriveRoundDto> getAllRounds(String driveId);  // ← added companyId

    List<ApplicationsDto> getAllApplications(String driveId);

    List<ApplicationRoundProjection> getApplicantsForDriveRound(String driveId, Integer roundNo);  // ← added companyId
    List<DriveDto>viewAllActiveDrives();
    String removeDrive(String driveId);
    String extendDriveApplication(String driveId, LocalDate localDate);
    String deleteDrive(String driveId);
    String deleteAdmin(Long id);
    List<AdminDto> getAllAdmins();


    // add to interface:
    ResponseEntity<byte[]> streamStudentResume(String rollNo);
}
