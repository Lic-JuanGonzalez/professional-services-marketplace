package com.marketplace.professional.hiring;

import com.marketplace.professional.hiring.dto.HireRequest;
import com.marketplace.professional.hiring.dto.HireResponse;
import com.marketplace.professional.hiring.dto.HireStatusUpdateRequest;
import com.marketplace.professional.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Hiring workflow: a client hires a professional for one of their published service
 * offerings, the professional accepts/rejects it, and the hire is eventually completed
 * or cancelled. All lookup, ownership and transition rules live in {@link HireService}.
 */
@RestController
@RequestMapping("/hires")
@RequiredArgsConstructor
public class HireController {

    private final HireService hireService;

    @PostMapping
    public ResponseEntity<HireResponse> create(@AuthenticationPrincipal CustomUserDetails principal,
                                                @Valid @RequestBody HireRequest request) {
        HireResponse response = hireService.createHire(principal, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/mine")
    public List<HireResponse> mine(@AuthenticationPrincipal CustomUserDetails principal) {
        return hireService.getMine(principal);
    }

    @GetMapping("/{id}")
    public HireResponse getById(@PathVariable Long id,
                                @AuthenticationPrincipal CustomUserDetails principal) {
        return hireService.getById(id, principal);
    }

    @PatchMapping("/{id}/status")
    public HireResponse updateStatus(@PathVariable Long id,
                                      @Valid @RequestBody HireStatusUpdateRequest request,
                                      @AuthenticationPrincipal CustomUserDetails principal) {
        return hireService.updateStatus(id, request, principal);
    }
}
