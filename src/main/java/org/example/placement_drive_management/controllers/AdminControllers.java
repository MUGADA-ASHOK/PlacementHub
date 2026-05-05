package org.example.placement_drive_management.controllers;

import org.example.placement_drive_management.dto.*;
import org.example.placement_drive_management.dto.auth.ApiResponse;
import org.example.placement_drive_management.service.AdminService;
import org.example.placement_drive_management.service.ApplicationRoundProjection;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin")
public class AdminControllers {

    private final AdminService adminService;

    public AdminControllers(AdminService adminService) {
        this.adminService = adminService;
    }

    // STUDENTS

    @GetMapping("/allStudents")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<PageResponse<StudentResponseDto>> getAllStudents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        return ResponseEntity.ok(adminService.getAllStudents(page, size));
    }

    @GetMapping("/allStudentProfiles")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<PageResponse<StudentProfileDto>> getAllStudentProfiles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.getAllProfiles(page, size));
    }

    @GetMapping("/student/{rollNo}/profile")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<StudentProfileDto> getStudentProfile(@PathVariable String rollNo) {
        return ResponseEntity.ok(adminService.getStudentProfileByRollNo(rollNo));
    }

    // DASHBOARD COUNT ENDPOINTS

    @GetMapping("/students/count")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<Long> countStudents() {
        return ResponseEntity.ok(adminService.countStudents());
    }

    @GetMapping("/companies/count")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<Long> countCompanies() {
        return ResponseEntity.ok(adminService.countCompanies());
    }

    @GetMapping("/activeDrives/count")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<Long> countActiveDrives() {
        return ResponseEntity.ok(adminService.countActiveDrives());
    }

    @GetMapping("/admins/count")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<Long> countAdmins() {
        return ResponseEntity.ok(adminService.countAdmins());
    }

    //COMPANIES

    @PostMapping("/company/addDrive")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<String> addDrive(@RequestBody DriveDto driveDto) {
        return ResponseEntity.ok(adminService.createDrive(driveDto));
    }

    @PostMapping("/company/addDriveEligibility")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<String> createEligibility(@RequestBody EligibilityDto eligibilityDto) {
        return ResponseEntity.ok(adminService.createEligibility(eligibilityDto));
    }

    @GetMapping("/allCompanies")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<PageResponse<CompanyDto>> getAllCompanies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.getAllCompanies(page, size));
    }

    @GetMapping("/company/{companyId}/drives")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<PageResponse<DriveDto>> getAllDrives(
            @PathVariable String companyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.getAllDrives(companyId, page, size));
    }

    //DRIVES

    @PutMapping("/publishDrives/{driveId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<String> publishDrives(@PathVariable String driveId) {
        return ResponseEntity.ok(adminService.publishDrivesToEligibleStudents(driveId));
    }

    @PutMapping("/closeDrive/{driveId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<String> closeDrive(@PathVariable String driveId) {
        return ResponseEntity.ok(adminService.closeDrive(driveId));
    }

    @GetMapping("/getAllActiveDrives")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<PageResponse<DriveDto>> getAllActiveDrives(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.viewAllActiveDrives(page, size));
    }

    @PutMapping("/removeDrive/{driveId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<String> removeDrive(@PathVariable String driveId) {
        return ResponseEntity.ok(adminService.removeDrive(driveId));
    }

    @PutMapping("/extendDrive/{driveId}/{localDate}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<String> extendDate(
            @PathVariable String driveId,
            @PathVariable LocalDate localDate) {
        return ResponseEntity.ok(adminService.extendDriveApplication(driveId, localDate));
    }

    @PutMapping("/updateDriveEligibility/{driveId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<String> updateEligibility(
            @PathVariable String driveId,
            @RequestBody EligibilityDto eligibilityDto) {
        return ResponseEntity.ok(adminService.updateEligibility(driveId, eligibilityDto));
    }

    @DeleteMapping("/deleteDrive/{driveId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<String> deleteDrive(@PathVariable String driveId) {
        return ResponseEntity.ok(adminService.deleteDrive(driveId));
    }

    // APPLICATIONS & ROUNDS

    @GetMapping("/student/allApplications/{rollNo}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<PageResponse<ApplicationsDto>> getAllApplicationsForStudent(
            @PathVariable String rollNo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.getAllApplicationsForaStudent(rollNo, page, size));
    }

    @GetMapping("/getAllApplications/{driveId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<PageResponse<ApplicationsDto>> getAllApplications(
            @PathVariable String driveId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.getAllApplications(driveId, page, size));
    }

    @GetMapping("/getAllDriveRounds/{driveId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<PageResponse<DriveRoundDto>> getAllRounds(
            @PathVariable String driveId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.getAllRounds(driveId, page, size));
    }

    @GetMapping("/getApplicantsForDriveRound/{driveId}/{roundNo}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<PageResponse<ApplicationRoundProjection>> getApplicantsForDriveRound(
            @PathVariable String driveId,
            @PathVariable Integer roundNo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.getApplicantsForDriveRound(driveId, roundNo, page, size));
    }

    // ── RESUME

    @GetMapping("/student/{rollNo}/viewResume")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<byte[]> viewStudentResume(@PathVariable String rollNo) {
        return adminService.streamStudentResume(rollNo);
    }
}