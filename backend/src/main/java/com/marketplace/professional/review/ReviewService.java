package com.marketplace.professional.review;

import com.marketplace.professional.domain.entity.Hire;
import com.marketplace.professional.domain.entity.ProfessionalProfile;
import com.marketplace.professional.domain.entity.Review;
import com.marketplace.professional.domain.enums.HireStatus;
import com.marketplace.professional.domain.repository.HireRepository;
import com.marketplace.professional.domain.repository.ProfessionalProfileRepository;
import com.marketplace.professional.domain.repository.ReviewRepository;
import com.marketplace.professional.exception.BadRequestException;
import com.marketplace.professional.exception.ResourceNotFoundException;
import com.marketplace.professional.review.dto.ReviewRequest;
import com.marketplace.professional.review.dto.ReviewResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final HireRepository hireRepository;
    private final ProfessionalProfileRepository professionalProfileRepository;

    @Transactional
    public ReviewResponse createReview(Long hireId, ReviewRequest request, Long reviewerId) {
        Hire hire = hireRepository.findById(hireId)
                .orElseThrow(() -> new ResourceNotFoundException("Hire not found: " + hireId));

        if (!hire.getClient().getId().equals(reviewerId)) {
            throw new AccessDeniedException("Only the hiring client can review this hire");
        }

        if (hire.getStatus() != HireStatus.COMPLETED) {
            throw new BadRequestException("Hire must be COMPLETED before it can be reviewed");
        }

        if (reviewRepository.existsByHireId(hireId)) {
            throw new BadRequestException("A review already exists for this hire");
        }

        Review review = Review.builder()
                .hire(hire)
                .reviewer(hire.getClient())
                .rating(request.rating())
                .comment(request.comment())
                .build();

        Review saved = reviewRepository.save(review);

        updateProfessionalRating(hire.getProfessional(), request.rating());

        log.info("Review {} created for hire {} by reviewer {}", saved.getId(), hireId, reviewerId);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsForProfessional(Long professionalId) {
        return reviewRepository.findByHire_ProfessionalId(professionalId).stream()
                .map(this::toResponse)
                .toList();
    }

    private void updateProfessionalRating(ProfessionalProfile professional, Integer rating) {
        double oldAvg = professional.getAvgRating();
        int oldCount = professional.getReviewCount();
        double newAvg = (oldAvg * oldCount + rating) / (oldCount + 1);

        professional.setAvgRating(newAvg);
        professional.setReviewCount(oldCount + 1);
        professionalProfileRepository.save(professional);
    }

    private ReviewResponse toResponse(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getHire().getId(),
                review.getReviewer().getId(),
                review.getReviewer().getFullName(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt()
        );
    }
}
