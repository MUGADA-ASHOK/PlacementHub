package org.example.placement_drive_management.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(indexes = {
        @Index(name = "idx_drive_company_id", columnList = "company_id"),
        @Index(name = "idx_drive_name", columnList = "drive_name"),
        @Index(name = "idx_drive_active_company", columnList = "company_id, is_active"),
        @Index(name = "idx_drive_registration_dates", columnList = "registration_start_date, registration_end_date")
})
public class Drive {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(name = "drive_id", unique = true)
    private String driveId;
    @Column(name = "drive_name")
    private String driveName;
    private String jobRole;
    private Double packageOffered;

    private String jobLocation;

    private Integer vacancies;
    @Column(name = "registration_start_date")
    private LocalDate registrationStartDate;
    @Column(name = "registration_end_date")
    private LocalDate registrationEndDate;
    private Integer maxRounds;
    private Boolean isActive=false;
    private String externalLink;

    @ManyToOne
    @JoinColumn(name = "company_id",referencedColumnName = "company_id")
    private Company company;
    private String description;
    @OneToOne(mappedBy = "drive", cascade = CascadeType.ALL,orphanRemoval = true)
    private Eligibility eligibility;

    @OneToMany(mappedBy = "drive", cascade = CascadeType.ALL,orphanRemoval = true)
    private List<Applications> applications;

    @OneToMany(mappedBy = "drive", cascade = CascadeType.ALL,orphanRemoval = true)
    private List<DriveRound> rounds;


}
