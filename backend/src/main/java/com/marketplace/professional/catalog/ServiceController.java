package com.marketplace.professional.catalog;

import com.marketplace.professional.catalog.dto.ServiceOfferingRequest;
import com.marketplace.professional.catalog.dto.ServiceOfferingResponse;
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
@RequestMapping("/services")
@RequiredArgsConstructor
@Tag(name = "Service Offerings", description = "Manage service offerings published by professionals")
public class ServiceController {

    private final ServiceOfferingService serviceOfferingService;

    @PostMapping
    @Operation(summary = "Create a service offering owned by the authenticated user's professional profile")
    public ResponseEntity<ServiceOfferingResponse> create(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody ServiceOfferingRequest request) {
        ServiceOfferingResponse response = serviceOfferingService.create(principal.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "List active service offerings, optionally filtered by category")
    public List<ServiceOfferingResponse> list(@RequestParam(required = false) String category) {
        return serviceOfferingService.list(category);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a service offering by id")
    public ServiceOfferingResponse getById(@PathVariable Long id) {
        return serviceOfferingService.getById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a service offering (owner or admin only)")
    public ServiceOfferingResponse update(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody ServiceOfferingRequest request) {
        return serviceOfferingService.update(id, principal, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft-delete a service offering (owner or admin only)")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal) {
        serviceOfferingService.delete(id, principal);
        return ResponseEntity.noContent().build();
    }
}
