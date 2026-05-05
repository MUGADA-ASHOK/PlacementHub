package org.example.placement_drive_management.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(indexes = {
        @Index(name="idx_driveRound_driveId_RoundNo",columnList = "drive_id,round_number")
})
public class DriveRound {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(name = "round_number")
    private Integer roundNumber;

    private String roundName;

    private LocalDate roundDate;

    private String roundLink;

    // A round belongs to a drive
    @ManyToOne
    @JoinColumn(name = "drive_id", referencedColumnName = "drive_id")
    private Drive drive;

    @OneToMany(mappedBy = "driveRound",cascade = CascadeType.ALL,orphanRemoval = true)
    private List<ApplicationRound> applicationRounds;
}