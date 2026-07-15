package com.marketplace.professional.catalog;

import com.marketplace.professional.catalog.dto.ProfessionalProfileRequest;
import com.marketplace.professional.catalog.dto.ProfessionalProfileResponse;
import com.marketplace.professional.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/professionals")
@RequiredArgsConstructor
@Tag(name = "Professional Profiles", description = "Manage professional profiles in the marketplace catalog")
public class ProfessionalController {

    private final ProfessionalProfileService professionalProfileService;

    @PostMapping
    @Operation(summary = "Create the authenticated user's professional profile")
    public ResponseEntity<ProfessionalProfileResponse> create(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody ProfessionalProfileRequest request) {
        ProfessionalProfileResponse response =
                professionalProfileService.create(principal.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "List professional profiles, optionally filtered by category")
    public List<ProfessionalProfileResponse> list(@RequestParam(required = false) String category) {
        return professionalProfileService.list(category);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a professional profile by id")
    public ProfessionalProfileResponse getById(@PathVariable Long id) {
        return professionalProfileService.getById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a professional profile (owner or admin only)")
    public ProfessionalProfileResponse update(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody ProfessionalProfileRequest request) {
        return professionalProfileService.update(id, principal, request);
    }
}
