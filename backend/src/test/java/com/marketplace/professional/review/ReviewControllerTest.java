package com.marketplace.professional.review;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.professional.domain.entity.User;
import com.marketplace.professional.domain.enums.Role;
import com.marketplace.professional.exception.BadRequestException;
import com.marketplace.professional.exception.ResourceNotFoundException;
import com.marketplace.professional.review.dto.ReviewRequest;
import com.marketplace.professional.review.dto.ReviewResponse;
import com.marketplace.professional.security.CustomUserDetails;
import com.marketplace.professional.security.JwtService;
import com.marketplace.professional.security.UserDetailsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewController.class)
@MockBean(JpaMetamodelMappingContext.class)
@ActiveProfiles("test")
class ReviewControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean ReviewService reviewService;
    @MockBean JwtService jwtService;
    @MockBean UserDetailsServiceImpl userDetailsService;

    private UsernamePasswordAuthenticationToken clientAuth;
    private ReviewResponse sampleResponse;

    @BeforeEach
    void setUp() {
        User clientUser = User.builder()
                .id(10L).fullName("Jane Client").email("jane@test.com")
                .password("hash").active(true).role(Role.CLIENT)
                .build();
        CustomUserDetails principal = new CustomUserDetails(clientUser);
        clientAuth = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());

        sampleResponse = new ReviewResponse(
                100L, 1L, 10L, "Jane Client", 5, "Great work", Instant.now());
    }

    @Test
    void createReview_withValidRequest_returns201() throws Exception {
        ReviewRequest request = new ReviewRequest(5, "Great work");

        when(reviewService.createReview(eq(1L), any(ReviewRequest.class), eq(10L)))
                .thenReturn(sampleResponse);

        mockMvc.perform(post("/reviews/hires/1")
                        .with(authentication(clientAuth)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.rating").value(5));
    }

    @Test
    void createReview_withInvalidRating_returns400() throws Exception {
        ReviewRequest request = new ReviewRequest(9, "too high");

        mockMvc.perform(post("/reviews/hires/1")
                        .with(authentication(clientAuth)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void createReview_whenHireNotCompleted_returns400() throws Exception {
        ReviewRequest request = new ReviewRequest(4, "ok");

        when(reviewService.createReview(eq(1L), any(ReviewRequest.class), eq(10L)))
                .thenThrow(new BadRequestException("Hire must be COMPLETED before it can be reviewed"));

        mockMvc.perform(post("/reviews/hires/1")
                        .with(authentication(clientAuth)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createReview_whenHireNotFound_returns404() throws Exception {
        ReviewRequest request = new ReviewRequest(4, "ok");

        when(reviewService.createReview(eq(1L), any(ReviewRequest.class), eq(10L)))
                .thenThrow(new ResourceNotFoundException("Hire not found: 1"));

        mockMvc.perform(post("/reviews/hires/1")
                        .with(authentication(clientAuth)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createReview_whenNotHireClient_returns403() throws Exception {
        ReviewRequest request = new ReviewRequest(4, "ok");

        when(reviewService.createReview(eq(1L), any(ReviewRequest.class), eq(10L)))
                .thenThrow(new AccessDeniedException("Only the hiring client can review this hire"));

        mockMvc.perform(post("/reviews/hires/1")
                        .with(authentication(clientAuth)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createReview_withoutAuth_returns401() throws Exception {
        ReviewRequest request = new ReviewRequest(5, "Great work");

        mockMvc.perform(post("/reviews/hires/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getReviewsForProfessional_returnsList() throws Exception {
        // Note: production SecurityConfig permits GET /reviews/** to everyone, but that
        // rule lives in SecurityConfig which is out of scope for this @WebMvcTest slice
        // (only the ReviewController web layer is loaded), so we authenticate here to
        // exercise the controller/service wiring independent of the security slice.
        when(reviewService.getReviewsForProfessional(anyLong()))
                .thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/reviews/professionals/20").with(authentication(clientAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100L))
                .andExpect(jsonPath("$[0].reviewerName").value("Jane Client"));
    }
}
