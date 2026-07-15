package com.marketplace.professional.domain.repository;

import com.marketplace.professional.domain.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Optional<Review> findByHireId(Long hireId);
    List<Review> findByHire_ProfessionalId(Long professionalId);
    boolean existsByHireId(Long hireId);
}
