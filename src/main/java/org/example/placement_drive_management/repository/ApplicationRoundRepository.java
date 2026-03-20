package org.example.placement_drive_management.repository;

import org.example.placement_drive_management.entity.ApplicationRound;
import org.example.placement_drive_management.entity.Applications;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ApplicationRoundRepository extends JpaRepository<ApplicationRound, Long> {


    @Query("""
    SELECT ar
    FROM applicationRound ar
    JOIN FETCH ar.application a
    JOIN FETCH a.student
    JOIN FETCH ar.driveRound dr
    WHERE dr.drive.driveId = :driveId
    AND dr.roundNumber = :roundNumber
""")
    List<ApplicationRound> findDetailedRoundApplicants(
            @Param("driveId") String driveId,
            @Param("roundNumber") Integer roundNumber
    );


    List<ApplicationRound> findByDriveRound_RoundNumber(Integer roundNumber);
    Optional<ApplicationRound> findByApplication_IdAndDriveRound_RoundNumber(
            Long applicationId, Integer roundNumber);

    @Query("SELECT ar FROM applicationRound ar " +
            "WHERE ar.driveRound.drive.driveId = :driveId " +
            "AND ar.driveRound.roundNumber = :roundNo " +
            "AND ar.score IS NOT NULL " +
            "ORDER BY ar.score DESC")
    List<ApplicationRound> findScoredStudentsOrderByScore(
            @Param("driveId") String driveId,
            @Param("roundNo") Integer roundNo);

    List<ApplicationRound> findByApplicationId(Long id);

    @Query("""
    SELECT ar
    FROM applicationRound ar
    JOIN FETCH ar.application a
    JOIN FETCH a.student s
    JOIN FETCH ar.driveRound dr
    WHERE dr.drive.driveId = :driveId
    AND s.rollNo = :rollNo
""")
    List<ApplicationRound> findAllRoundDetails(
            @Param("driveId") String driveId,
            @Param("rollNo") String rollNo
    );
}
