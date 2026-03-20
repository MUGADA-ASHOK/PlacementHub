package org.example.placement_drive_management.mappers;

import org.example.placement_drive_management.dto.ApplicationsDto;
import org.example.placement_drive_management.entity.Applications;

public class ApplicationsMapper {
    public static ApplicationsDto mapToApplicationDto(Applications applications) {
        return new ApplicationsDto(
                applications.getId(),
                applications.getStudent().getRollNo(),
                applications.getDrive().getDriveId(),
                applications.getStatus(),
                applications.getCurrentRoundNumber(),
                applications.getAppliedDate(),
                false,
                null
        );
    }
}
