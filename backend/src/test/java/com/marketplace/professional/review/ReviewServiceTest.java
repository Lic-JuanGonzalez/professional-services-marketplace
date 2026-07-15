package com.marketplace.professional.review;

import com.marketplace.professional.domain.entity.Hire;
import com.marketplace.professional.domain.entity.ProfessionalProfile;
import com.marketplace.professional.domain.entity.Review;
import com.marketplace.professional.domain.entity.User;
import com.marketplace.professional.domain.enums.HireStatus;
import com.marketplace.professional.domain.repository.HireRepository;
import com.marketplace.professional.domain.repository.ProfessionalProfileRepository;
import com.marketplace.professional.domain.repository.ReviewRepository;
import com.marketplace.professional.exception.BadRequestException;
import com.marketplace.professional.exception.ResourceNotFoundException;
import com.marketplace.professional.review.dto.ReviewRequest;
import com.marketplace.professional.review.dto.ReviewResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock ReviewRepository reviewRepository;
    @Mock HireRepository hireRepository;
    @Mock ProfessionalProfileRepository professionalProfileRepository;

    private ReviewService reviewService;

    private User client;
    private ProfessionalProfile professional;
    private Hire hire;

    @BeforeEach
    void setUp() {
        reviewService = new ReviewService(reviewRepository, hireRepository, professionalProfileRepository);

        client = User.builder().id(10L).fullName("Jane Client").email("jane@test.com").build();

        professional = ProfessionalProfile.builder()
                .id(20L)
                .avgRating(0.0)
                .reviewCount(0)
                .build();

        hire = Hire.builder()
                .id(1L)
                .client(client)
                .professional(professional)
                .status(HireStatus.COMPLETED)
                .build();
    }

    @Test
    void createReview_withValidData_recalculatesAvgRatingFromZero() {
        ReviewRequest request = new ReviewRequest(5, "Great work");

        when(hireRepository.findById(1L)).thenReturn(Optional.of(hire));
        when(reviewRepository.existsByHireId(1L)).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
            Review r = invocation.getArgument(0);
            r.setId(100L);
            r.setCreatedAt(java.time.Instant.now());
            return r;
        });

        ReviewResponse response = reviewService.createReview(1L, request, 10L);

        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.hireId()).isEqualTo(1L);
        assertThat(response.reviewerId()).isEqualTo(10L);
        assertThat(response.rating()).isEqualTo(5);

        ArgumentCaptor<ProfessionalProfile> captor = ArgumentCaptor.forClass(ProfessionalProfile.class);
        verify(professionalProfileRepository).save(captor.capture());
        ProfessionalProfile saved = captor.getValue();
        assertThat(saved.getAvgRating()).isEqualTo(5.0);
        assertThat(saved.getReviewCount()).isEqualTo(1);
    }

    @Test
    void createReview_withExistingRatings_recalculatesWeightedAverage() {
        professional.setAvgRating(4.0);
        professional.setReviewCount(3);

        ReviewRequest request = new ReviewRequest(2, null);

        when(hireRepository.findById(1L)).thenReturn(Optional.of(hire));
        when(reviewRepository.existsByHireId(1L)).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
            Review r = invocation.getArgument(0);
            r.setId(101L);
            r.setCreatedAt(java.time.Instant.now());
            return r;
        });

        reviewService.createReview(1L, request, 10L);

        // (4.0 * 3 + 2) / 4 = 3.5
        ArgumentCaptor<ProfessionalProfile> captor = ArgumentCaptor.forClass(ProfessionalProfile.class);
        verify(professionalProfileRepository).save(captor.capture());
        ProfessionalProfile saved = captor.getValue();
        assertThat(saved.getAvgRating()).isEqualTo(3.5);
        assertThat(saved.getReviewCount()).isEqualTo(4);
    }

    @Test
    void createReview_whenHireNotCompleted_throwsBadRequestException() {
        hire.setStatus(HireStatus.ACCEPTED);
        ReviewRequest request = new ReviewRequest(4, "ok");

        when(hireRepository.findById(1L)).thenReturn(Optional.of(hire));

        assertThatThrownBy(() -> reviewService.createReview(1L, request, 10L))
                .isInstanceOf(BadRequestException.class);

        verify(reviewRepository, never()).save(any());
        verify(professionalProfileRepository, never()).save(any());
    }

    @Test
    void createReview_whenReviewAlreadyExists_throwsBadRequestException() {
        ReviewRequest request = new ReviewRequest(3, "meh");

        when(hireRepository.findById(1L)).thenReturn(Optional.of(hire));
        when(reviewRepository.existsByHireId(1L)).thenReturn(true);

        assertThatThrownBy(() -> reviewService.createReview(1L, request, 10L))
                .isInstanceOf(BadRequestException.class);

        verify(reviewRepository, never()).save(any());
        verify(professionalProfileRepository, never()).save(any());
    }

    @Test
    void createReview_whenReviewerIsNotHireClient_throwsAccessDeniedException() {
        ReviewRequest request = new ReviewRequest(5, "nice");

        when(hireRepository.findById(1L)).thenReturn(Optional.of(hire));

        assertThatThrownBy(() -> reviewService.createReview(1L, request, 999L))
                .isInstanceOf(AccessDeniedException.class);

        verify(reviewRepository, never()).save(any());
        verify(professionalProfileRepository, never()).save(any());
    }

    @Test
    void createReview_whenHireNotFound_throwsResourceNotFoundException() {
        ReviewRequest request = new ReviewRequest(5, "nice");

        when(hireRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.createReview(1L, request, 10L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
