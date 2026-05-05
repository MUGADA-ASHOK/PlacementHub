package org.example.placement_drive_management.repository;

import jakarta.transaction.Transactional;
import org.example.placement_drive_management.entity.Applications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Applications, Long> {

    Page<Applications> findByDrive_DriveId(String driveId,Pageable pageable);

    Page<Applications> findByStudent_RollNo(String rollNo, Pageable pageable);
    @Query("select ap from Applications ap join fetch ap.drive d join fetch ap.student s where d.driveId = :driveId and s.rollNo = :rollNo")
    Optional<Applications> findByDrive_DriveIdAndStudent_RollNo(
            @Param("driveId") String driveId,
            @Param("rollNo") String rollNo
    );
    List<Applications> findByDrive_DriveIdAndStatus(String driveId, String status);
    // All applications for a student filtered by status
    List<Applications> findByStudent_RollNoAndStatus(String rollNo, String status);

    @Modifying
    @Transactional
    void deleteByDrive_DriveId(String driveId);
    boolean existsByStudent_RollNoAndDrive_Company_CompanyId(
            String rollNo, String companyId);
    @Query(
            value = """
            SELECT ap FROM Applications ap
            JOIN FETCH ap.drive d
            JOIN FETCH d.company c
            WHERE ap.student.rollNo = :rollNo
            AND ap.status <> :status
        """,
            countQuery = """
            SELECT COUNT(ap) FROM Applications ap
            WHERE ap.student.rollNo = :rollNo
            AND ap.status <> 'ELIGIBLE'
        """
    )
    Page<Applications> findByApplicationsByStudentRollNoByStatus(
            @Param("rollNo") String rollNo,
            Pageable pageable,
            String status
    );
    @Query(
            value = """
            SELECT ap FROM Applications ap
            JOIN FETCH ap.drive d
            JOIN FETCH d.company c
            WHERE ap.student.rollNo = :rollNo
            AND ap.status = :status
        """,
            countQuery = """
            SELECT COUNT(ap) FROM Applications ap
            WHERE ap.student.rollNo = :rollNo
            AND ap.status = 'ELIGIBLE'
        """
    )
    Page<Applications> findByApplicationsByStudentRollNoByStatusEligible(
            @Param("rollNo") String rollNo,
            Pageable pageable,
            String status
    );
    @Query("""
    SELECT ap FROM Applications ap
    JOIN FETCH ap.drive d
    JOIN FETCH ap.student s
    WHERE d.driveId = :driveId
    AND s.rollNo = :rollNo
    AND ap.status = 'ELIGIBLE'
""")
    Optional<Applications> findEligibleApplicationForApply(
            @Param("driveId") String driveId,
            @Param("rollNo") String rollNo
    );
    long countByStudent_RollNoAndStatus(String rollNo, String status);
}