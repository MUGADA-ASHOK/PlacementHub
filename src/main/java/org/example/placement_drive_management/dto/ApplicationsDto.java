package org.example.placement_drive_management.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApplicationsDto {
    private Long id;

    private String studentRollNo;
    private String driveId;

    private String status;

    private Integer currentRound;

    private LocalDate appliedAt;
    private Boolean offerAccepted;
    private DriveInfoDto driveInfo;
}


