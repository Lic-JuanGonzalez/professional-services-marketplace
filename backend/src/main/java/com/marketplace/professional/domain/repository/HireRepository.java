package com.marketplace.professional.domain.repository;

import com.marketplace.professional.domain.entity.Hire;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HireRepository extends JpaRepository<Hire, Long> {
    List<Hire> findByClientId(Long clientId);
    List<Hire> findByProfessionalId(Long professionalId);
}
