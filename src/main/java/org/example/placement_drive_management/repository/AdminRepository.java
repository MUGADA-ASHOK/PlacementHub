package org.example.placement_drive_management.repository;

import org.example.placement_drive_management.entity.Admin;
import org.example.placement_drive_management.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin,Long> {
    Optional<Admin> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByRole(Role role);
    Optional<Admin> findById(Long id);
    @Query("SELECT a FROM Admin a where a.role <> 'ROLE_SUPER_ADMIN' ")
    Page<Admin> findAdmins(Pageable pageable);
}
