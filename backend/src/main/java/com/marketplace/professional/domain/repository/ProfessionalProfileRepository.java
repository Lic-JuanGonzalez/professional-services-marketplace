package com.marketplace.professional.domain.repository;

import com.marketplace.professional.domain.entity.ProfessionalProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProfessionalProfileRepository extends JpaRepository<ProfessionalProfile, Long> {
    Optional<ProfessionalProfile> findByUserId(Long userId);

    @Query("select p from ProfessionalProfile p where (:category is null or p.category = :category)")
    java.util.List<ProfessionalProfile> search(@Param("category") String category);
}
