package com.marketplace.professional.review.dto;

import java.time.Instant;

public record ReviewResponse(
        Long id,
        Long hireId,
        Long reviewerId,
        String reviewerName,
        Integer rating,
        String comment,
        Instant createdAt
) {
}
