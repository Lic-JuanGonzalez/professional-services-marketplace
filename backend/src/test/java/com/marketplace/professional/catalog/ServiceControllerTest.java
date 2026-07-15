package com.marketplace.professional.catalog;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.professional.catalog.dto.ServiceOfferingRequest;
import com.marketplace.professional.catalog.dto.ServiceOfferingResponse;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ServiceController.class)
@Import(SecurityConfig.class)
@MockBean(JpaMetamodelMappingContext.class)
class ServiceControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean ServiceOfferingService serviceOfferingService;
    @MockBean UserRepository userRepository;
    @MockBean JwtService jwtService;
    @MockBean UserDetailsServiceImpl userDetailsService;

    private UsernamePasswordAuthenticationToken ownerAuth;
    private UsernamePasswordAuthenticationToken otherUserAuth;
    private ServiceOfferingResponse sampleResponse;

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

        sampleResponse = new ServiceOfferingResponse(
                20L, 10L, "Senior Developer",
                "Website build", "Full website build", "Software",
                new BigDecimal("500.00"), true,
                Instant.now(), Instant.now()
        );
    }

    @Test
    void create_withValidRequest_returns201() throws Exception {
        ServiceOfferingRequest req = new ServiceOfferingRequest(
                "Website build", "Full website build", "Software", new BigDecimal("500.00"));

        when(serviceOfferingService.create(eq(1L), any())).thenReturn(sampleResponse);

        mockMvc.perform(post("/services")
                        .with(authentication(ownerAuth)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Website build"));
    }

    @Test
    void create_whenNoProfessionalProfile_returns400() throws Exception {
        ServiceOfferingRequest req = new ServiceOfferingRequest(
                "Website build", "Full website build", "Software", new BigDecimal("500.00"));

        when(serviceOfferingService.create(eq(1L), any()))
                .thenThrow(new BadRequestException(
                        "You must create a professional profile before adding service offerings"));

        mockMvc.perform(post("/services")
                        .with(authentication(ownerAuth)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void list_isPublic() throws Exception {
        when(serviceOfferingService.list(null)).thenReturn(java.util.List.of(sampleResponse));

        mockMvc.perform(get("/services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Website build"));
    }

    @Test
    void getById_isPublic() throws Exception {
        when(serviceOfferingService.getById(20L)).thenReturn(sampleResponse);

        mockMvc.perform(get("/services/20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(20));
    }

    @Test
    void update_byNonOwner_returns403() throws Exception {
        ServiceOfferingRequest req = new ServiceOfferingRequest(
                "Updated title", "Updated desc", "Software", new BigDecimal("600.00"));

        when(serviceOfferingService.update(eq(20L), any(), any()))
                .thenThrow(new AccessDeniedException("You do not have permission to modify this service offering"));

        mockMvc.perform(put("/services/20")
                        .with(authentication(otherUserAuth)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    void update_byOwner_returns200() throws Exception {
        ServiceOfferingRequest req = new ServiceOfferingRequest(
                "Updated title", "Updated desc", "Software", new BigDecimal("600.00"));

        ServiceOfferingResponse updated = new ServiceOfferingResponse(
                20L, 10L, "Senior Developer",
                "Updated title", "Updated desc", "Software",
                new BigDecimal("600.00"), true,
                Instant.now(), Instant.now()
        );

        when(serviceOfferingService.update(eq(20L), any(), any())).thenReturn(updated);

        mockMvc.perform(put("/services/20")
                        .with(authentication(ownerAuth)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated title"));
    }
}
