package com.marketplace.professional.domain.repository;

import com.marketplace.professional.domain.entity.ServiceOffering;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceOfferingRepository extends JpaRepository<ServiceOffering, Long> {
    List<ServiceOffering> findByProfessionalId(Long professionalId);
    List<ServiceOffering> findByCategoryAndActiveTrue(String category);
    List<ServiceOffering> findByActiveTrue();
}
