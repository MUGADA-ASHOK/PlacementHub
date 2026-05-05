package org.example.placement_drive_management.controllers;

import org.example.placement_drive_management.dto.ApplicationRoundDto;
import org.example.placement_drive_management.dto.ApplicationsDto;
import org.example.placement_drive_management.dto.PageResponse;
import org.example.placement_drive_management.dto.StudentProfileDto;
import org.example.placement_drive_management.entity.Student;
import org.example.placement_drive_management.service.StudentProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/student/profile")   // ← FIXED: was /api/auth/profile
public class StudentProfileController {

    private final StudentProfileService studentProfileService;

    public StudentProfileController(StudentProfileService studentProfileService) {
        this.studentProfileService = studentProfileService;
    }

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    public ResponseEntity<String> addStudentProfile(
            @RequestBody StudentProfileDto studentProfileDto,@AuthenticationPrincipal Student student) {
        String message = studentProfileService.createStudentProfile(studentProfileDto,student.getRollNo());
        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }

    @PutMapping("/update")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    public ResponseEntity<String> updateStudentProfile(
            @RequestBody StudentProfileDto studentProfileDto,
            @AuthenticationPrincipal Student student) {
        String message = studentProfileService.updateStudentProfile(studentProfileDto,student.getRollNo());
        return ResponseEntity.ok(message);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_STUDENT', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<StudentProfileDto> getStudentProfile(@AuthenticationPrincipal Student student) {
        return ResponseEntity.ok(studentProfileService.getStudentProfile(student.getRollNo()));
    }

    @GetMapping("/allApplications")
    @PreAuthorize("hasAnyAuthority('ROLE_STUDENT', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<PageResponse<ApplicationsDto>> getAllApplicationsOfStudent(

            @AuthenticationPrincipal Student student,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20")  int size) {
        return ResponseEntity.ok(studentProfileService.getAllApplicationsForStudent(student.getRollNo(),page,size));
    }

    @PutMapping("/applyDrive/{driveId}")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    public ResponseEntity<String> applyDrive(@PathVariable String driveId, @AuthenticationPrincipal Student student) {
        return ResponseEntity.status(HttpStatus.OK).body(studentProfileService.applyDrive(driveId,student.getRollNo()));
    }

    @GetMapping("/allRounds/{driveId}")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    public ResponseEntity<List<ApplicationRoundDto>> getAllRoundsForDrive(@PathVariable String driveId,@AuthenticationPrincipal Student student) {
        return ResponseEntity.ok(studentProfileService.getAllApplicationRoundsForStudentAndDriveId(driveId,student.getRollNo()));
    }

    @GetMapping("/allEligibleApplications")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    public ResponseEntity<PageResponse<ApplicationsDto>> getAllEligibleApplications(@AuthenticationPrincipal Student student,@RequestParam(defaultValue = "0") int  page, @RequestParam(defaultValue = "20")  int size) {
        return ResponseEntity.ok(studentProfileService.getAllEligibleApplications(student.getRollNo(),page,size));
    }
    @PostMapping("/uploadResume")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    public ResponseEntity<String> uploadResume(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal Student student) {
        return ResponseEntity.ok(
                studentProfileService.uploadResume(file, student.getEmail())
        );
    }
    /**
     * GET /api/student/profile/viewResume
     *
     * Proxies the student's resume PDF from Cloudinary and serves it with
     * Content-Disposition: inline so the browser renders it inside an <iframe>
     * instead of downloading it.
     *
     * This is needed because Cloudinary serves resource_type=raw files with
     * Content-Disposition: attachment by default, which forces a download
     * instead of an inline iframe preview.
     */
    @GetMapping("/profile/viewResume")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    public ResponseEntity<byte[]> viewResume(
            @AuthenticationPrincipal Student student) {
        return studentProfileService.streamResume(student.getUsername());
    }

    @GetMapping("/getEligibleDrives")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    public ResponseEntity<Long> countEligibleDrives(@AuthenticationPrincipal Student student) {
        return ResponseEntity.ok(studentProfileService.countEligibleDrives(student.getRollNo()));
    }

    @GetMapping("/getAppliedDrives")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    public ResponseEntity<Long> countAppliedDrives(@AuthenticationPrincipal Student student) {
        return ResponseEntity.ok(studentProfileService.countApplicationsByStatus(student.getRollNo(), "APPLIED"));
    }

    @GetMapping("/getSelectedDrives")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    public ResponseEntity<Long> countSelectedDrives(@AuthenticationPrincipal Student student) {
        return ResponseEntity.ok(studentProfileService.countApplicationsByStatus(student.getRollNo(), "SELECTED"));
    }

    @GetMapping("/getInProcessDrives")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    public ResponseEntity<Long> countInProcessDrives(@AuthenticationPrincipal Student student) {
        return ResponseEntity.ok(studentProfileService.countApplicationsByStatus(student.getRollNo(), "INPROCESS"));
    }
}