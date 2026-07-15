package com.marketplace.professional.review;

import com.marketplace.professional.review.dto.ReviewRequest;
import com.marketplace.professional.review.dto.ReviewResponse;
import com.marketplace.professional.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Client reviews and ratings for professionals")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/hires/{hireId}")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Leave a review for a completed hire",
            description = "Only the client on the hire may review it, and only once the hire is COMPLETED.")
    public ReviewResponse createReview(
            @PathVariable Long hireId,
            @Valid @RequestBody ReviewRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return reviewService.createReview(hireId, request, principal.getUserId());
    }

    @GetMapping("/professionals/{professionalId}")
    @Operation(summary = "List all reviews for a professional")
    public List<ReviewResponse> getReviewsForProfessional(@PathVariable Long professionalId) {
        return reviewService.getReviewsForProfessional(professionalId);
    }
}
