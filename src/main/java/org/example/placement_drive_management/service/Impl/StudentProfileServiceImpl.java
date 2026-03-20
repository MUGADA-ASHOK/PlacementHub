package org.example.placement_drive_management.service.Impl;

import jakarta.transaction.Transactional;
import org.example.placement_drive_management.dto.*;
import org.example.placement_drive_management.entity.*;
import org.example.placement_drive_management.exceptions.ResourceNotFoundException;
import org.example.placement_drive_management.exceptions.UnauthorizedAccessException;
import org.example.placement_drive_management.mappers.ApplicationRoundMapper;
import org.example.placement_drive_management.mappers.ApplicationsMapper;
import org.example.placement_drive_management.mappers.DriveRoundMapper;
import org.example.placement_drive_management.repository.*;
import org.example.placement_drive_management.service.StudentProfileService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

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
    public StudentProfileServiceImpl(StudentProfileRepository studentProfileRepository, StudentRepository studentRepository,ApplicationRepository applicationRepository,DriveRepository driveRepository, ApplicationRoundRepository applicationRoundRepository) {
        this.studentProfileRepository = studentProfileRepository;
        this.studentRepository = studentRepository;
        this.applicationRepository=applicationRepository;
        this.driveRepository = driveRepository;
        this.applicationRoundRepository=applicationRoundRepository;
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
        StudentProfile studentProfileDto=studentProfileRepository.findByStudentRollNo(rollNo)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Student with RollNo "
                                + rollNo + " not found"));
        return new StudentProfileDto(
                studentProfileDto.getId(),
                studentProfileDto.getDepartment(),
                studentProfileDto.getTenthPercentage(),
                studentProfileDto.getTwelthPercentage(),
                studentProfileDto.getDiplomaPercentage(),
                studentProfileDto.getCurrentCgpa(),
                studentProfileDto.getCurrentSemester(),
                studentProfileDto.getBacklogCount(),
                studentProfileDto.getHasbackloghistory(),
                studentProfileDto.getPassingYear(),
                studentProfileDto.getGender()
        );
    }
    @Override
    public List<ApplicationsDto> getAllApplicationsForStudent(String  studentRollNo) {
        StudentProfile studentProfile= studentProfileRepository.findByStudentRollNo(studentRollNo).orElseThrow(()->new ResourceNotFoundException("Student with Roll No :"+studentRollNo+"not found"));
        List<Applications> getApplications = studentProfile.getApplicationsList();
        List<ApplicationsDto> applicationsDtos = new ArrayList<>();
        for(Applications application: getApplications){
            if(application.getStatus().equals("ELIGIBLE")) {
                continue;
            }
            ApplicationsDto applicationsDto = ApplicationsMapper.mapToApplicationDto(application);
            Drive drive = application.getDrive();
            DriveInfoDto driveInfoDto = new DriveInfoDto();
            driveInfoDto.setCompanyName(drive.getCompany().getCompanyName());
            driveInfoDto.setRole(drive.getJobRole());
            driveInfoDto.setPackageAmount(drive.getPackageOffered());
            applicationsDto.setDriveInfo(driveInfoDto);
            applicationsDtos.add(applicationsDto);
        }
        return applicationsDtos;
    }

    @Override
    public List<ApplicationRoundDto> getAllApplicationRoundsForStudentAndDriveId(String driveId, String rollNumberInContext) {
        List<ApplicationRound>applicationRounds = applicationRoundRepository.findAllRoundDetails(driveId, rollNumberInContext);
        return applicationRounds.stream().map(ApplicationRoundMapper::maptoApplicationRoundDto).collect(Collectors.toList());
    }

    @Override
    public String applyDrive(String driveId,String rollNo) {
        Applications application = applicationRepository.findByDrive_DriveIdAndStudent_RollNo(driveId,rollNo).orElseThrow(()-> new ResourceNotFoundException("application not found"));
        if(application.getStatus().equals("APPLIED")) {
            return "You have already applied this application";
        }
        Drive drive = driveRepository.findByDriveId(driveId).orElseThrow(()->new ResourceNotFoundException("Drive with DriveId not found"));
        LocalDate start = drive.getRegistrationStartDate();
        LocalDate end = drive.getRegistrationEndDate();
        if(LocalDate.now().isBefore(start) || LocalDate.now().isAfter(end)) {
            return "Application time is over";
        }
        application.setAppliedDate(LocalDate.now());
        application.setCurrentRoundNumber(0);
        application.setStatus("APPLIED");
        application.setExternalApplied(false);
        applicationRepository.save(application);
        return "application applied for "+driveId+" successfully";
    }
}
