package org.example.placement_drive_management.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.*;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name="applicationRound")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationRound {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Pattern(regexp = "^(CLEARED|FAILED|PENDING|SELECTED)$",message = "Status should be CLEARED  | FAILED | PENDING|SELECTED")
    private String status;
    private Double score;
    private String feedback;

    @ManyToOne
    @JoinColumn(name = "application_id", nullable = false)
    private Applications application;

    @ManyToOne
    @JoinColumn(name = "drive_round_id", nullable = false)
    private DriveRound driveRound;
}