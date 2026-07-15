package com.marketplace.professional.catalog.dto;

import com.marketplace.professional.domain.entity.ServiceOffering;

import java.math.BigDecimal;
import java.time.Instant;

public record ServiceOfferingResponse(
        Long id,
        Long professionalId,
        String professionalHeadline,
        String title,
        String description,
        String category,
        BigDecimal price,
        Boolean active,
        Instant createdAt,
        Instant updatedAt
) {

    public static ServiceOfferingResponse from(ServiceOffering offering) {
        var professional = offering.getProfessional();
        return new ServiceOfferingResponse(
                offering.getId(),
                professional.getId(),
                professional.getHeadline(),
                offering.getTitle(),
                offering.getDescription(),
                offering.getCategory(),
                offering.getPrice(),
                offering.getActive(),
                offering.getCreatedAt(),
                offering.getUpdatedAt()
        );
    }
}
