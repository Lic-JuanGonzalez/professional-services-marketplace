package com.marketplace.professional.hiring.dto;

import com.marketplace.professional.domain.enums.HireStatus;

import java.time.Instant;

/**
 * Read model for a hire, exposing only the identifiers and denormalized fields a client needs.
 */
public record HireResponse(
        Long id,
        Long clientId,
        Long professionalId,
        Long serviceOfferingId,
        String serviceTitle,
        HireStatus status,
        Instant scheduledDate,
        String notes,
        Instant createdAt
) {
}
