package com.marketplace.professional.catalog;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.professional.catalog.dto.ProfessionalProfileRequest;
import com.marketplace.professional.catalog.dto.ProfessionalProfileResponse;
import com.marketplace.professional.config.SecurityConfig;
import com.marketplace.professional.domain.entity.User;
import com.marketplace.professional.domain.enums.Role;
import com.marketplace.professional.domain.repository.UserRepository;
import com.marketplace.professional.exception.BadRequestException;
import com.marketplace.professional.security.CustomUserDetails;
import com.marketplace.professional.security.JwtService;
import com.marketplace.professional.security.UserDetailsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProfessionalController.class)
@Import(SecurityConfig.class)
@MockBean(JpaMetamodelMappingContext.class)
class ProfessionalControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean ProfessionalProfileService professionalProfileService;
    @MockBean UserRepository userRepository;
    @MockBean JwtService jwtService;
    @MockBean UserDetailsServiceImpl userDetailsService;

    private UsernamePasswordAuthenticationToken ownerAuth;
    private UsernamePasswordAuthenticationToken otherUserAuth;
    private ProfessionalProfileResponse sampleResponse;

    @BeforeEach
    void setUp() {
        User owner = User.builder()
                .id(1L).fullName("Jane Doe").email("jane@test.com")
                .password("hash").role(Role.PROFESSIONAL).active(true).build();
        CustomUserDetails ownerPrincipal = new CustomUserDetails(owner);
        ownerAuth = new UsernamePasswordAuthenticationToken(
                ownerPrincipal, null, ownerPrincipal.getAuthorities());

        User otherUser = User.builder()
                .id(2L).fullName("Other User").email("other@test.com")
                .password("hash").role(Role.PROFESSIONAL).active(true).build();
        CustomUserDetails otherPrincipal = new CustomUserDetails(otherUser);
        otherUserAuth = new UsernamePasswordAuthenticationToken(
                otherPrincipal, null, otherPrincipal.getAuthorities());

        sampleResponse = new ProfessionalProfileResponse(
                10L, 1L, "Jane Doe", "jane@test.com",
                "Senior Developer", "Experienced dev", "Software", "Remote",
                new BigDecimal("50.00"), 0.0, 0, false,
                Instant.now(), Instant.now()
        );
    }

    @Test
    void create_withValidRequest_returns201() throws Exception {
        ProfessionalProfileRequest req = new ProfessionalProfileRequest(
                "Senior Developer", "Experienced dev", "Software", "Remote", new BigDecimal("50.00"));

        when(professionalProfileService.create(eq(1L), any())).thenReturn(sampleResponse);

        mockMvc.perform(post("/professionals")
                        .with(authentication(ownerAuth)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.headline").value("Senior Developer"));
    }

    @Test
    void create_whenProfileAlreadyExists_returns400() throws Exception {
        ProfessionalProfileRequest req = new ProfessionalProfileRequest(
                "Senior Developer", "Experienced dev", "Software", "Remote", new BigDecimal("50.00"));

        when(professionalProfileService.create(eq(1L), any()))
                .thenThrow(new BadRequestException("A professional profile already exists for this user"));

        mockMvc.perform(post("/professionals")
                        .with(authentication(ownerAuth)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void list_isPublic() throws Exception {
        when(professionalProfileService.list(null)).thenReturn(java.util.List.of(sampleResponse));

        mockMvc.perform(get("/professionals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].headline").value("Senior Developer"));
    }

    @Test
    void getById_isPublic() throws Exception {
        when(professionalProfileService.getById(10L)).thenReturn(sampleResponse);

        mockMvc.perform(get("/professionals/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void update_byNonOwner_returns403() throws Exception {
        ProfessionalProfileRequest req = new ProfessionalProfileRequest(
                "Updated headline", "Updated bio", "Software", "Remote", new BigDecimal("75.00"));

        when(professionalProfileService.update(eq(10L), any(), any()))
                .thenThrow(new AccessDeniedException("You do not have permission to modify this professional profile"));

        mockMvc.perform(put("/professionals/10")
                        .with(authentication(otherUserAuth)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    void update_byOwner_returns200() throws Exception {
        ProfessionalProfileRequest req = new ProfessionalProfileRequest(
                "Updated headline", "Updated bio", "Software", "Remote", new BigDecimal("75.00"));

        ProfessionalProfileResponse updated = new ProfessionalProfileResponse(
                10L, 1L, "Jane Doe", "jane@test.com",
                "Updated headline", "Updated bio", "Software", "Remote",
                new BigDecimal("75.00"), 0.0, 0, false,
                Instant.now(), Instant.now()
        );

        when(professionalProfileService.update(eq(10L), any(), any())).thenReturn(updated);

        mockMvc.perform(put("/professionals/10")
                        .with(authentication(ownerAuth)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.headline").value("Updated headline"));
    }
}
