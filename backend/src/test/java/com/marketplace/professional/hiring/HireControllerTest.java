package com.marketplace.professional.hiring;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.professional.domain.entity.User;
import com.marketplace.professional.domain.enums.HireStatus;
import com.marketplace.professional.domain.enums.Role;
import com.marketplace.professional.domain.repository.UserRepository;
import com.marketplace.professional.hiring.dto.HireRequest;
import com.marketplace.professional.hiring.dto.HireResponse;
import com.marketplace.professional.hiring.dto.HireStatusUpdateRequest;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HireController.class)
@MockBean(JpaMetamodelMappingContext.class)
@ActiveProfiles("test")
class HireControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean HireService hireService;
    @MockBean UserRepository userRepository;
    @MockBean JwtService jwtService;
    @MockBean UserDetailsServiceImpl userDetailsService;

    private Authentication clientAuth;
    private Authentication professionalAuth;
    private HireResponse sampleResponse;

    @BeforeEach
    void setUp() {
        User clientUser = User.builder()
                .id(1L).fullName("Client").email("client@test.com")
                .password("hash").role(Role.CLIENT).active(true).build();
        CustomUserDetails clientPrincipal = new CustomUserDetails(clientUser);
        clientAuth = new UsernamePasswordAuthenticationToken(
                clientPrincipal, null, clientPrincipal.getAuthorities());

        User professionalUser = User.builder()
                .id(2L).fullName("Pro").email("pro@test.com")
                .password("hash").role(Role.PROFESSIONAL).active(true).build();
        CustomUserDetails professionalPrincipal = new CustomUserDetails(professionalUser);
        professionalAuth = new UsernamePasswordAuthenticationToken(
                professionalPrincipal, null, professionalPrincipal.getAuthorities());

        sampleResponse = new HireResponse(
                100L, 1L, 10L, 20L, "Fix a leak",
                HireStatus.PENDING, null, "please help", Instant.now());
    }

    @Test
    void create_asClient_returns201() throws Exception {
        HireRequest request = new HireRequest(20L, null, "please help");
        when(hireService.createHire(any(), any())).thenReturn(sampleResponse);

        mockMvc.perform(post("/hires")
                        .with(authentication(clientAuth)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.serviceTitle").value("Fix a leak"));
    }

    @Test
    void create_missingServiceOfferingId_returns422() throws Exception {
        HireRequest request = new HireRequest(null, null, null);

        mockMvc.perform(post("/hires")
                        .with(authentication(clientAuth)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void create_withoutAuth_returns401() throws Exception {
        HireRequest request = new HireRequest(20L, null, null);

        mockMvc.perform(post("/hires")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void mine_asClient_returnsList() throws Exception {
        when(hireService.getMine(any())).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/hires/mine").with(authentication(clientAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100));
    }

    @Test
    void getById_found_returnsHire() throws Exception {
        when(hireService.getById(eq(100L), any())).thenReturn(sampleResponse);

        mockMvc.perform(get("/hires/100").with(authentication(clientAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100));
    }

    @Test
    void updateStatus_valid_returnsUpdatedHire() throws Exception {
        HireStatusUpdateRequest request = new HireStatusUpdateRequest(HireStatus.ACCEPTED);
        HireResponse accepted = new HireResponse(
                100L, 1L, 10L, 20L, "Fix a leak",
                HireStatus.ACCEPTED, null, "please help", Instant.now());
        when(hireService.updateStatus(eq(100L), any(), any())).thenReturn(accepted);

        mockMvc.perform(patch("/hires/100/status")
                        .with(authentication(professionalAuth)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }

    @Test
    void updateStatus_missingStatus_returns422() throws Exception {
        String body = "{}";

        mockMvc.perform(patch("/hires/100/status")
                        .with(authentication(professionalAuth)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnprocessableEntity());
    }
}
