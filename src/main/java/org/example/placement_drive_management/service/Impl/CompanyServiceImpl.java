package org.example.placement_drive_management.service.Impl;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.example.placement_drive_management.dto.*;
import org.example.placement_drive_management.entity.*;
import org.example.placement_drive_management.exceptions.ResourceNotFoundException;
import org.example.placement_drive_management.exceptions.UnauthorizedAccessException;
import org.example.placement_drive_management.mappers.*;
import org.example.placement_drive_management.repository.*;
import org.example.placement_drive_management.service.ApplicationRoundProjection;
import org.example.placement_drive_management.service.CompanyService;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional
public class CompanyServiceImpl implements CompanyService {

    private DriveRoundRepository driveRoundRepository;
    private DriveRepository driveRepository;
    private ApplicationRepository applicationRepository;
    private ApplicationRoundRepository applicationRoundRepository;
    private CompanyRepository companyRepository;
    private CloudinaryService cloudinaryService;
    private StudentProfileRepository studentProfileRepository;

    private void verifyDriveOwnership(String driveId, String companyId) {
        Drive drive = driveRepository.findByDriveId(driveId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Drive not found: " + driveId));
        if (!drive.getCompany().getCompanyId().equals(companyId)) {
            throw new UnauthorizedAccessException(
                    "You do not have permission to access this drive.");
        }
    }
    private void closeIfFinalRound(Drive drive) {
        drive.setIsActive(false);
    }

    @Override
    public String publishDriveRound(String driveId, DriveRoundDto dto, String companyId) {

        Drive drive = driveRepository.findByDriveId(driveId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Drive not found: " + driveId));

        if (!drive.getCompany().getCompanyId().equals(companyId)) {
            throw new AccessDeniedException(
                    "You do not have permission to publish rounds for this drive.");
        }

        boolean roundExists = driveRoundRepository
                .existsByDrive_DriveIdAndRoundNumber(driveId, dto.getRoundNumber());
        if (roundExists) {
            return "Round " + dto.getRoundNumber() + " already exists for this drive.";
        }

        DriveRound driveRound = DriveRoundMapper.mapToDriveRound(dto);
        driveRound.setDrive(drive);
        driveRoundRepository.save(driveRound);

        String requiredStatus = (dto.getRoundNumber() == 1) ? "APPLIED" : "INPROCESS";
        List<Applications> eligibleApplications = applicationRepository
                .findByDrive_DriveIdAndStatus(driveId, requiredStatus);

        if (eligibleApplications.isEmpty()) {
            return "No eligible students found for round " + dto.getRoundNumber();
        }

        List<ApplicationRound> applicationRounds = new ArrayList<>();
        for (Applications application : eligibleApplications) {
            application.setCurrentRoundNumber(dto.getRoundNumber());
            ApplicationRound applicationRound = new ApplicationRound();
            applicationRound.setDriveRound(driveRound);
            applicationRound.setApplication(application);
            applicationRound.setStatus("PENDING");
            applicationRounds.add(applicationRound);
        }

        applicationRoundRepository.saveAll(applicationRounds);

        return "Round " + dto.getRoundNumber() + " published for "
                + eligibleApplications.size() + " students successfully.";
    }

    @Override
    public PageResponse<DriveDto> getAllDrives(String companyId, int page,int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("registrationEndDate").descending());
        Page<DriveDto> drives = driveRepository.findByCompany_CompanyId(companyId,pageable).map(drive-> DriveMapper.maptoDriveDto(drive));
        return PageMapper.mapToPageResponse(drives);
    }

    @Override
    public List<DriveRoundDto> getAllRounds(String driveId, String companyId) {
        verifyDriveOwnership(driveId, companyId);
        List<DriveRound> driveRounds = driveRoundRepository
                .findByDrive_DriveId(driveId);
        return driveRounds.stream()
                .map(DriveRoundMapper::mapToDriveRoundDto)
                .collect(Collectors.toList());
    }

    @Override
    public PageResponse<ApplicationsDto> getAllApplications(String driveId, String companyId,int page,int size) {
        verifyDriveOwnership(driveId, companyId);
        Pageable pageable = PageRequest.of(page, size,Sort.by("appliedDate").ascending());
        Page<ApplicationsDto> applications = applicationRepository
                .findByDrive_DriveId(driveId,pageable).map(application->ApplicationsMapper.mapToApplicationDto(application));

        return PageMapper.mapToPageResponse(applications);
    }

    @Override
    public PageResponse<ApplicationRoundProjection> getApplicantsForDriveRound(
            String driveId, Integer roundNo, String companyId,int page,int size) {
        verifyDriveOwnership(driveId, companyId);
        Pageable pageable = PageRequest.of(page,size,Sort.by("score").descending());
        Page<ApplicationRoundProjection> roundEntries = applicationRoundRepository
                .findApplicantsProjected(driveId, roundNo,pageable);
        return PageMapper.mapToPageResponse(roundEntries);

    }

    @Override
    public String publishScoreForDriveRound(String driveId, String rollNo,
                                            Integer roundNo, Double score,
                                            String companyId) {
        verifyDriveOwnership(driveId, companyId);
        Applications application = applicationRepository
                .findByDrive_DriveIdAndStudent_RollNo(driveId, rollNo)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Application not found for rollNo: " + rollNo));
        ApplicationRound applicationRound = applicationRoundRepository
                .findByApplication_IdAndDriveRound_RoundNumber(
                        application.getId(), roundNo)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "ApplicationRound not found for round: " + roundNo));
        applicationRound.setScore(score);
        applicationRoundRepository.save(applicationRound);
        return "Score published for RollNo: " + rollNo + " | Round: " + roundNo;
    }
    @Override
    public String publishFeedback(String driveId, String rollNo,
                                  Integer roundNo, String feedback,
                                  String companyId) {
        verifyDriveOwnership(driveId, companyId);
        Applications application = applicationRepository
                .findByDrive_DriveIdAndStudent_RollNo(driveId, rollNo)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
        ApplicationRound applicationRound = applicationRoundRepository
                .findByApplication_IdAndDriveRound_RoundNumber(application.getId(), roundNo)
                .orElseThrow(() -> new ResourceNotFoundException("Round not found"));
        applicationRound.setFeedback(feedback);
        applicationRoundRepository.save(applicationRound);
        return "Feedback saved for " + rollNo;
    }

    @Override
    public String filterTopKStudents(String driveId, Integer roundNo,
                                     Integer topK, String companyId) {
        verifyDriveOwnership(driveId, companyId);

        List<ApplicationRound> studentApplications = applicationRoundRepository
                .findScoredStudentsOrderByScore(driveId, roundNo);

        if (studentApplications.isEmpty()) {
            throw new ResourceNotFoundException("No scored students found for this round.");
        }
        if (topK <= 0 || topK > studentApplications.size()) {
            throw new IllegalArgumentException(
                    "Invalid topK. Must be between 1 and " + studentApplications.size());
        }
        List<ApplicationRound> roundsToUpdate = new ArrayList<>();
        List<Applications> applicationsToUpdate = new ArrayList<>();
        Drive drive = driveRepository.findByDriveId(driveId).orElseThrow(() -> new ResourceNotFoundException("Drive not found for drive id: " + driveId));
        if(drive.getMaxRounds().equals(roundNo)) {
            closeIfFinalRound(drive);
        }
        String appRoundstatus = drive.getMaxRounds().equals(roundNo)? "SELECTED":"CLEARED";
        String appStatus = drive.getMaxRounds().equals(roundNo)? "SELECTED":"INPROCESS";
        for (int i = 0; i < studentApplications.size(); i++) {
            ApplicationRound appRound = studentApplications.get(i);
            Applications application = appRound.getApplication();

            if (i < topK) {
                appRound.setStatus(appRoundstatus);
                application.setStatus(appStatus);
            } else {
                appRound.setStatus("FAILED");
                application.setStatus("REJECTED");
            }
            application.setCurrentRoundNumber(roundNo);
            roundsToUpdate.add(appRound);
            applicationsToUpdate.add(application);
        }

        applicationRoundRepository.saveAll(roundsToUpdate);
        applicationRepository.saveAll(applicationsToUpdate);

        return "Top " + topK + " students cleared out of "
                + studentApplications.size() + " scored students.";
    }

    @Override
    public String filterByCutOffMarks(String driveId, Integer roundNo,
                                      Double cutOff, String companyId) {
        verifyDriveOwnership(driveId, companyId);

        List<ApplicationRound> studentApplications = applicationRoundRepository
                .findScoredStudentsOrderByScore(driveId, roundNo);

        if (studentApplications.isEmpty()) {
            throw new ResourceNotFoundException("No scored students found for this round.");
        }
        Drive drive = driveRepository.findByDriveId(driveId).orElseThrow(() -> new ResourceNotFoundException("Drive not found for drive id: " + driveId));
        if(drive.getMaxRounds().equals(roundNo)) {
            closeIfFinalRound(drive);
        }
        String appRoundstatus = drive.getMaxRounds().equals(roundNo)? "SELECTED":"CLEARED";
        String appStatus = drive.getMaxRounds().equals(roundNo)? "SELECTED":"INPROCESS";
        List<ApplicationRound> roundsToUpdate = new ArrayList<>();
        List<Applications> applicationsToUpdate = new ArrayList<>();
        int cleared = 0, failed = 0;

        for (ApplicationRound appRound : studentApplications) {
            Applications application = appRound.getApplication();

            if (appRound.getScore() >= cutOff) {
                appRound.setStatus(appRoundstatus);
                application.setStatus(appStatus);
                cleared++;
            } else {
                appRound.setStatus("FAILED");
                application.setStatus("REJECTED");
                failed++;
            }
            application.setCurrentRoundNumber(roundNo);
            roundsToUpdate.add(appRound);
            applicationsToUpdate.add(application);
        }

        applicationRoundRepository.saveAll(roundsToUpdate);
        applicationRepository.saveAll(applicationsToUpdate);

        return "Cutoff: " + cutOff + " | Cleared: " + cleared + " | Failed: " + failed;
    }

    @Override
    public Integer countFilterByCutOffMarks(String driveId, Integer roundNo, Double cutOffMarks, String companyId) {
        verifyDriveOwnership(driveId, companyId);
        List<ApplicationRound>applicationRounds = applicationRoundRepository.findScoredStudentsOrderByScore(driveId, roundNo);
        int count = 0;
        for (ApplicationRound applicationRound : applicationRounds) {
            if(applicationRound.getScore() >= cutOffMarks) {
                count++;
            }
            else{
                break;
            }
        }
        return count;
    }
    // Add cloudinaryService injection:
// private final CloudinaryService cloudinaryService;  ← add to constructor

    @Override
    public ResponseEntity<byte[]> streamStudentResume(String rollNo, String companyEmail) {
        // Find the company
        Company company = companyRepository.findByEmail(companyEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Company not found: " + companyEmail));

        // Verify the student has actually applied to one of this company's drives
        boolean hasApplication = applicationRepository
                .existsByStudent_RollNoAndDrive_Company_CompanyId(
                        rollNo, company.getCompanyId());
        if (!hasApplication) {
            throw new UnauthorizedAccessException(
                    "No application found for this student in your drives.");
        }

        StudentProfile profile = studentProfileRepository.findByStudentRollNo(rollNo)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Profile not found for rollNo: " + rollNo));
        if (profile.getResumeUrl() == null) {
            return ResponseEntity.notFound().build();
        }
        try {
            byte[] bytes = cloudinaryService.fetchResumeBytes(profile.getResumeUrl());
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            String filename = rollNo + "_resume.pdf";
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"");
            return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
        } catch (IOException e) {
            throw new RuntimeException("Failed to fetch resume: " + e.getMessage(), e);
        }
    }
    @Override
    public long countTotalDrives(String companyId) {
        return driveRepository.countByCompany_CompanyId(companyId);
    }

    @Override
    public long countActiveDrives(String companyId) {
        return driveRepository.countByCompany_CompanyIdAndIsActive(companyId, true);
    }
}