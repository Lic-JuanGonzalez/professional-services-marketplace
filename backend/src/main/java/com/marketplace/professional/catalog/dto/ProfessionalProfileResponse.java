package com.marketplace.professional.catalog.dto;

import com.marketplace.professional.domain.entity.ProfessionalProfile;

import java.math.BigDecimal;
import java.time.Instant;

public record ProfessionalProfileResponse(
        Long id,
        Long userId,
        String fullName,
        String email,
        String headline,
        String bio,
        String category,
        String location,
        BigDecimal hourlyRate,
        Double avgRating,
        Integer reviewCount,
        Boolean verified,
        Instant createdAt,
        Instant updatedAt
) {

    public static ProfessionalProfileResponse from(ProfessionalProfile profile) {
        var user = profile.getUser();
        return new ProfessionalProfileResponse(
                profile.getId(),
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                profile.getHeadline(),
                profile.getBio(),
                profile.getCategory(),
                profile.getLocation(),
                profile.getHourlyRate(),
                profile.getAvgRating(),
                profile.getReviewCount(),
                profile.getVerified(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }
}
