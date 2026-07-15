package com.marketplace.professional.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.professional.auth.dto.AuthResponse;
import com.marketplace.professional.auth.dto.LoginRequest;
import com.marketplace.professional.auth.dto.RegisterRequest;
import com.marketplace.professional.domain.enums.Role;
import com.marketplace.professional.domain.repository.UserRepository;
import com.marketplace.professional.exception.BadRequestException;
import com.marketplace.professional.security.JwtService;
import com.marketplace.professional.security.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// SecurityConfig is a plain @Configuration (no @Controller/@ControllerAdvice stereotype),
// so @WebMvcTest does not load it and Boot's default security auto-config would otherwise
// deny every request. addFilters = false disables the servlet filter chain for this slice
// so the publicly-exposed /auth/** endpoints can be exercised directly; JwtService/
// UserDetailsServiceImpl are still mocked because JwtAuthFilter (a Filter bean) is
// constructed by the context regardless.
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@MockBean(JpaMetamodelMappingContext.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void register_withValidRequest_returns201() throws Exception {
        RegisterRequest request = new RegisterRequest("Jane Doe", "jane@example.com", "password123", Role.CLIENT);
        AuthResponse response = new AuthResponse("access-token", "Bearer", 1L, "jane@example.com", "Jane Doe", Role.CLIENT);

        when(authService.register(any())).thenReturn(response);

        mockMvc.perform(post("/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.email").value("jane@example.com"))
                .andExpect(jsonPath("$.role").value("CLIENT"));
    }

    @Test
    void register_withDuplicateEmail_returns400() throws Exception {
        RegisterRequest request = new RegisterRequest("Jane Doe", "jane@example.com", "password123", Role.CLIENT);

        when(authService.register(any()))
                .thenThrow(new BadRequestException("Email is already registered: jane@example.com"));

        mockMvc.perform(post("/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_withInvalidPayload_returns422() throws Exception {
        RegisterRequest request = new RegisterRequest("", "not-an-email", "short", Role.CLIENT);

        mockMvc.perform(post("/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void login_withValidCredentials_returns200() throws Exception {
        LoginRequest request = new LoginRequest("jane@example.com", "password123");
        AuthResponse response = new AuthResponse("access-token", "Bearer", 1L, "jane@example.com", "Jane Doe", Role.CLIENT);

        when(authService.login(any())).thenReturn(response);

        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.userId").value(1));
    }

    @Test
    void login_withBadCredentials_returns401() throws Exception {
        LoginRequest request = new LoginRequest("jane@example.com", "wrong-password");

        when(authService.login(any())).thenThrow(new BadCredentialsException("Invalid credentials"));

        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
