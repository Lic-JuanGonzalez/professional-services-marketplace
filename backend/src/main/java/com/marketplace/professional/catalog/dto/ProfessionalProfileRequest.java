package com.marketplace.professional.catalog.dto;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record ProfessionalProfileRequest(
        @NotBlank(message = "headline is required") String headline,
        String bio,
        @NotBlank(message = "category is required") String category,
        String location,
        BigDecimal hourlyRate
) {
}
