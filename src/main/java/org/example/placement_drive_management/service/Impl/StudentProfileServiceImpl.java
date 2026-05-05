package org.example.placement_drive_management.service.Impl;

import jakarta.transaction.Transactional;
import org.example.placement_drive_management.dto.*;
import org.example.placement_drive_management.entity.*;
import org.example.placement_drive_management.exceptions.ResourceNotFoundException;
import org.example.placement_drive_management.exceptions.UnauthorizedAccessException;
import org.example.placement_drive_management.mappers.*;
import org.example.placement_drive_management.repository.*;
import org.example.placement_drive_management.service.StudentProfileService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class StudentProfileServiceImpl implements StudentProfileService {

    private final StudentProfileRepository studentProfileRepository;
    private final StudentRepository studentRepository;
    private final ApplicationRepository applicationRepository;
    private final DriveRepository driveRepository;
    private final ApplicationRoundRepository applicationRoundRepository;
    private final CloudinaryService cloudinaryService;
    public StudentProfileServiceImpl(StudentProfileRepository studentProfileRepository, StudentRepository studentRepository,ApplicationRepository applicationRepository,DriveRepository driveRepository, ApplicationRoundRepository applicationRoundRepository, CloudinaryService cloudinaryService) {
        this.studentProfileRepository = studentProfileRepository;
        this.studentRepository = studentRepository;
        this.applicationRepository=applicationRepository;
        this.driveRepository = driveRepository;
        this.applicationRoundRepository=applicationRoundRepository;
        this.cloudinaryService = cloudinaryService;
    }

    @Override
    public String createStudentProfile( StudentProfileDto studentProfileDto,String  rollNo) {
        Student student = studentRepository.findByRollNo(rollNo)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Student with RollNo "
                                + rollNo + " not found"));

        StudentProfile studentProfile = new StudentProfile();
                studentProfile.setStudent((student));
                studentProfile.setDepartment(studentProfileDto.getDepartment());
                studentProfile.setCurrentSemester(studentProfileDto.getCurrentSemester());
                studentProfile.setHasbackloghistory(studentProfileDto.getHasBacklogHistory());
                studentProfile.setBacklogCount(studentProfileDto.getBacklogCount());
                studentProfile.setCurrentCgpa(studentProfileDto.getCurrentCgpa());
                studentProfile.setTenthPercentage(studentProfileDto.getTenthPercentage());
                studentProfile.setDiplomaPercentage(studentProfileDto.getDiplomaPercentage());
                studentProfile.setTwelthPercentage(studentProfileDto.getTwelthPercentage());
                studentProfile.setGender(studentProfileDto.getGender());
                studentProfile.setPassingYear(studentProfileDto.getPassingYear());
        studentProfileRepository.save(studentProfile);

        return "Profile Created Successfully";
    }



    @Override
    public String updateStudentProfile(
                                       StudentProfileDto studentProfileDto,String rollNo) {

        StudentProfile existingProfile =
                studentProfileRepository.findByStudentRollNo(rollNo)
                        .orElseThrow(() ->
                                new ResourceNotFoundException("Student with RollNo "
                                        + rollNo + " not found"));

        existingProfile.setDepartment(studentProfileDto.getDepartment());
        existingProfile.setTenthPercentage(studentProfileDto.getTenthPercentage());
        existingProfile.setTwelthPercentage(studentProfileDto.getTwelthPercentage());
        existingProfile.setDiplomaPercentage(studentProfileDto.getDiplomaPercentage());
        existingProfile.setCurrentCgpa(studentProfileDto.getCurrentCgpa());
        existingProfile.setCurrentSemester(studentProfileDto.getCurrentSemester());
        existingProfile.setBacklogCount(studentProfileDto.getBacklogCount());
        existingProfile.setHasbackloghistory(studentProfileDto.getHasBacklogHistory());
        existingProfile.setGender(studentProfileDto.getGender());
        existingProfile.setPassingYear(studentProfileDto.getPassingYear());
        studentProfileRepository.save(existingProfile);

        return "Profile Updated Successfully";
    }
    @Override
    public StudentProfileDto getStudentProfile(String rollNo) {
        StudentProfile studentProfile=studentProfileRepository.findByStudentRollNo(rollNo)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Student with RollNo "
                                + rollNo + " not found"));
        return StudentProfileMapper.maptoStudentProfileDto(studentProfile);
    }
    @Override
    public PageResponse<ApplicationsDto> getAllApplicationsForStudent(String studentRollNo, int page, int size) {
        studentRepository.findByRollNo(studentRollNo).orElseThrow(() -> new ResourceNotFoundException("Student with Roll No: " + studentRollNo + " not found"));
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("appliedDate").descending()
        );
        Page<Applications> applicationsPage = applicationRepository.findByApplicationsByStudentRollNoByStatus(studentRollNo, pageable,"ELIGIBLE");
        Page<ApplicationsDto> dtoPage = applicationsPage.map(application -> {
            ApplicationsDto applicationsDto = ApplicationsMapper.mapToApplicationDto(application);
            Drive drive = application.getDrive();
            DriveInfoDto driveInfoDto = new DriveInfoDto();
            driveInfoDto.setCompanyName(drive.getCompany().getCompanyName());
            driveInfoDto.setRole(drive.getJobRole());
            driveInfoDto.setPackageAmount(drive.getPackageOffered());
            applicationsDto.setDriveInfo(driveInfoDto);
            return applicationsDto;
        });
        return PageMapper.mapToPageResponse(dtoPage);
    }

    @Override
    public List<ApplicationRoundDto> getAllApplicationRoundsForStudentAndDriveId(String driveId, String rollNumberInContext) {
        List<ApplicationRound>applicationRounds = applicationRoundRepository.findAllRoundDetails(driveId, rollNumberInContext);
        return applicationRounds.stream().map(ApplicationRoundMapper::maptoApplicationRoundDto).collect(Collectors.toList());
    }

    @Override
    public String applyDrive(String driveId, String rollNo) {

        Applications application = applicationRepository
                .findEligibleApplicationForApply(driveId, rollNo)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Eligible application not found for driveId: " + driveId
                ));

        Drive drive = application.getDrive();

        LocalDate start = drive.getRegistrationStartDate();
        LocalDate end = drive.getRegistrationEndDate();

        LocalDate today = LocalDate.now();

        if (today.isBefore(start) || today.isAfter(end)) {
            return "Application time is over";
        }

        application.setAppliedDate(today);
        application.setCurrentRoundNumber(0);
        application.setStatus("APPLIED");
        application.setExternalApplied(false);

        applicationRepository.save(application);

        return "Application applied for " + driveId + " successfully";
    }

    @Override
    public PageResponse<ApplicationsDto> getAllEligibleApplications(String rollNo,int page,int size){
        studentRepository.findByRollNo(rollNo).orElseThrow(()->new ResourceNotFoundException("Student with Roll No :"+rollNo+"not found"));
        Pageable pageable = PageRequest.of(page,size,Sort.by("appliedDate").descending());
        Page<ApplicationsDto> applicationsDtos = applicationRepository.findByApplicationsByStudentRollNoByStatusEligible(rollNo,pageable,"ELIGIBLE").map(application -> {
            ApplicationsDto applicationsDto = ApplicationsMapper.mapToApplicationDto(application);
            Drive drive = application.getDrive();
            DriveInfoDto driveInfoDto = new DriveInfoDto();
            driveInfoDto.setCompanyName(drive.getCompany().getCompanyName());
            driveInfoDto.setRole(drive.getJobRole());
            driveInfoDto.setPackageAmount(drive.getPackageOffered());
            applicationsDto.setDriveInfo(driveInfoDto);
            return applicationsDto;
        } );
        return PageMapper.mapToPageResponse(applicationsDtos);
    }
    private Student getStudentByEmail(String email) {
        return studentRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student not found with email: " + email));
    }
    private StudentProfile getProfileByEmail(String email) {
        Student student = getStudentByEmail(email);
        return studentProfileRepository.findByStudentRollNo(student.getRollNo())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Profile not found for student: " + email));
    }
    @Override
    public String uploadResume(MultipartFile file, String email) {
        if (file == null || file.isEmpty())
            throw new IllegalArgumentException("File must not be empty");

        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals("application/pdf"))
            throw new IllegalArgumentException("Only PDF files are accepted");

        if (file.getSize() > 5 * 1024 * 1024)
            throw new IllegalArgumentException("File size must be under 5 MB");

        StudentProfile profile = getProfileByEmail(email);
        Student student = profile.getStudent();

        try {
            // resource_type=raw → PDF stored verbatim, never garbled on download
            // public_id=rollNo  → re-uploads overwrite same file, no duplicates
            String secureUrl = cloudinaryService.uploadResume(file, student.getRollNo());
            profile.setResumeUrl(secureUrl);
            studentProfileRepository.save(profile);
            return "Resume uploaded successfully";
        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to upload resume: " + e.getMessage(), e);
        }
    }

    @Override
    public ResponseEntity<byte[]> streamResume(String email) {
        StudentProfile profile = getProfileByEmail(email);
        if (profile.getResumeUrl() == null) {
            return ResponseEntity.notFound().build();
        }
        try {
            byte[] bytes = cloudinaryService.fetchResumeBytes(profile.getResumeUrl());
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            // inline = render in browser, not download
            headers.setContentDispositionFormData("inline", "resume.pdf");
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"resume.pdf\"");
            return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
        } catch (IOException e) {
            throw new RuntimeException("Failed to fetch resume: " + e.getMessage(), e);
        }
    }
    @Override
    public long countEligibleDrives(String rollNo) {
        return applicationRepository.countByStudent_RollNoAndStatus(rollNo, "ELIGIBLE");
    }

    @Override
    public long countApplicationsByStatus(String rollNo, String status) {
        return applicationRepository.countByStudent_RollNoAndStatus(rollNo, status);
    }
}
