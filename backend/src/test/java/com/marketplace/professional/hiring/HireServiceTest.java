package com.marketplace.professional.hiring;

import com.marketplace.professional.domain.entity.Hire;
import com.marketplace.professional.domain.entity.ProfessionalProfile;
import com.marketplace.professional.domain.entity.ServiceOffering;
import com.marketplace.professional.domain.entity.User;
import com.marketplace.professional.domain.enums.HireStatus;
import com.marketplace.professional.domain.enums.Role;
import com.marketplace.professional.domain.repository.HireRepository;
import com.marketplace.professional.domain.repository.ProfessionalProfileRepository;
import com.marketplace.professional.domain.repository.ServiceOfferingRepository;
import com.marketplace.professional.domain.repository.UserRepository;
import com.marketplace.professional.exception.BadRequestException;
import com.marketplace.professional.exception.ResourceNotFoundException;
import com.marketplace.professional.hiring.dto.HireRequest;
import com.marketplace.professional.hiring.dto.HireResponse;
import com.marketplace.professional.hiring.dto.HireStatusUpdateRequest;
import com.marketplace.professional.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HireServiceTest {

    @Mock private HireRepository hireRepository;
    @Mock private ServiceOfferingRepository serviceOfferingRepository;
    @Mock private ProfessionalProfileRepository professionalProfileRepository;
    @Mock private UserRepository userRepository;

    private HireService hireService;

    private static final Long CLIENT_ID = 1L;
    private static final Long PROFESSIONAL_USER_ID = 2L;
    private static final Long ADMIN_USER_ID = 3L;
    private static final Long OTHER_USER_ID = 99L;

    private User client;
    private User professionalUser;
    private ProfessionalProfile professionalProfile;
    private ServiceOffering serviceOffering;

    @BeforeEach
    void setUp() {
        hireService = new HireService(hireRepository, serviceOfferingRepository, professionalProfileRepository, userRepository);

        client = User.builder().id(CLIENT_ID).email("client@test.com").role(Role.CLIENT).active(true).build();
        professionalUser = User.builder().id(PROFESSIONAL_USER_ID).email("pro@test.com").role(Role.PROFESSIONAL).active(true).build();

        professionalProfile = ProfessionalProfile.builder()
                .id(10L)
                .user(professionalUser)
                .headline("Plumber")
                .category("Home")
                .build();

        serviceOffering = ServiceOffering.builder()
                .id(20L)
                .professional(professionalProfile)
                .title("Fix a leak")
                .category("Home")
                .price(new BigDecimal("50.00"))
                .active(true)
                .build();
    }

    private Hire hireWithStatus(HireStatus status) {
        return Hire.builder()
                .id(100L)
                .client(client)
                .professional(professionalProfile)
                .serviceOffering(serviceOffering)
                .status(status)
                .createdAt(Instant.now())
                .build();
    }

    private CustomUserDetails principalFor(User user) {
        return new CustomUserDetails(user);
    }

    // ---- createHire ----

    @Test
    void createHire_asClient_createsPendingHire() {
        HireRequest request = new HireRequest(20L, null, "please help");
        when(serviceOfferingRepository.findById(20L)).thenReturn(Optional.of(serviceOffering));
        when(userRepository.findById(CLIENT_ID)).thenReturn(Optional.of(client));
        when(hireRepository.save(any(Hire.class))).thenAnswer(inv -> {
            Hire h = inv.getArgument(0);
            h.setId(100L);
            h.setCreatedAt(Instant.now());
            return h;
        });

        HireResponse response = hireService.createHire(principalFor(client), request);

        assertThat(response.status()).isEqualTo(HireStatus.PENDING);
        assertThat(response.clientId()).isEqualTo(CLIENT_ID);
        assertThat(response.professionalId()).isEqualTo(10L);
        assertThat(response.serviceOfferingId()).isEqualTo(20L);
    }

    @Test
    void createHire_asNonClient_throwsAccessDenied() {
        HireRequest request = new HireRequest(20L, null, null);

        assertThatThrownBy(() -> hireService.createHire(principalFor(professionalUser), request))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void createHire_offeringNotFound_throwsNotFound() {
        HireRequest request = new HireRequest(999L, null, null);
        when(serviceOfferingRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hireService.createHire(principalFor(client), request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createHire_inactiveOffering_throwsNotFound() {
        serviceOffering.setActive(false);
        HireRequest request = new HireRequest(20L, null, null);
        when(serviceOfferingRepository.findById(20L)).thenReturn(Optional.of(serviceOffering));

        assertThatThrownBy(() -> hireService.createHire(principalFor(client), request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ---- getById ----

    @Test
    void getById_asClientOwner_returnsHire() {
        Hire hire = hireWithStatus(HireStatus.PENDING);
        when(hireRepository.findById(100L)).thenReturn(Optional.of(hire));

        HireResponse response = hireService.getById(100L, principalFor(client));

        assertThat(response.id()).isEqualTo(100L);
    }

    @Test
    void getById_asUnrelatedUser_throwsAccessDenied() {
        Hire hire = hireWithStatus(HireStatus.PENDING);
        when(hireRepository.findById(100L)).thenReturn(Optional.of(hire));
        User other = User.builder().id(OTHER_USER_ID).email("other@test.com").role(Role.CLIENT).active(true).build();

        assertThatThrownBy(() -> hireService.getById(100L, principalFor(other)))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void getById_notFound_throwsNotFound() {
        when(hireRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hireService.getById(404L, principalFor(client)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ---- transition matrix ----

    static Stream<Arguments> validTransitions() {
        return Stream.of(
                Arguments.of(HireStatus.PENDING, HireStatus.ACCEPTED, "professional"),
                Arguments.of(HireStatus.PENDING, HireStatus.REJECTED, "professional"),
                Arguments.of(HireStatus.ACCEPTED, HireStatus.COMPLETED, "professional"),
                Arguments.of(HireStatus.PENDING, HireStatus.CANCELLED, "client")
        );
    }

    @ParameterizedTest
    @MethodSource("validTransitions")
    void updateStatus_validTransition_succeeds(HireStatus from, HireStatus to, String actor) {
        Hire hire = hireWithStatus(from);
        when(hireRepository.findById(100L)).thenReturn(Optional.of(hire));
        when(hireRepository.save(any(Hire.class))).thenAnswer(inv -> inv.getArgument(0));

        User actingUser = actor.equals("professional") ? professionalUser : client;
        HireResponse response = hireService.updateStatus(100L, new HireStatusUpdateRequest(to), principalFor(actingUser));

        assertThat(response.status()).isEqualTo(to);
    }

    @ParameterizedTest
    @MethodSource("validTransitions")
    void updateStatus_validTransition_asAdmin_succeeds(HireStatus from, HireStatus to, String actor) {
        Hire hire = hireWithStatus(from);
        when(hireRepository.findById(100L)).thenReturn(Optional.of(hire));
        when(hireRepository.save(any(Hire.class))).thenAnswer(inv -> inv.getArgument(0));

        User admin = User.builder().id(ADMIN_USER_ID).email("admin@test.com").role(Role.ADMIN).active(true).build();
        HireResponse response = hireService.updateStatus(100L, new HireStatusUpdateRequest(to), principalFor(admin));

        assertThat(response.status()).isEqualTo(to);
    }

    static Stream<Arguments> invalidTransitions() {
        return Stream.of(
                Arguments.of(HireStatus.PENDING, HireStatus.COMPLETED),
                Arguments.of(HireStatus.ACCEPTED, HireStatus.REJECTED),
                Arguments.of(HireStatus.ACCEPTED, HireStatus.CANCELLED),
                Arguments.of(HireStatus.REJECTED, HireStatus.ACCEPTED),
                Arguments.of(HireStatus.COMPLETED, HireStatus.PENDING),
                Arguments.of(HireStatus.CANCELLED, HireStatus.ACCEPTED)
        );
    }

    @ParameterizedTest
    @MethodSource("invalidTransitions")
    void updateStatus_invalidTransition_throwsBadRequest(HireStatus from, HireStatus to) {
        Hire hire = hireWithStatus(from);
        when(hireRepository.findById(100L)).thenReturn(Optional.of(hire));

        User admin = User.builder().id(ADMIN_USER_ID).email("admin@test.com").role(Role.ADMIN).active(true).build();

        assertThatThrownBy(() -> hireService.updateStatus(100L, new HireStatusUpdateRequest(to), principalFor(admin)))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void updateStatus_clientTriesToAccept_throwsAccessDenied() {
        Hire hire = hireWithStatus(HireStatus.PENDING);
        when(hireRepository.findById(100L)).thenReturn(Optional.of(hire));

        assertThatThrownBy(() -> hireService.updateStatus(
                100L, new HireStatusUpdateRequest(HireStatus.ACCEPTED), principalFor(client)))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void updateStatus_professionalTriesToCancel_throwsAccessDenied() {
        Hire hire = hireWithStatus(HireStatus.PENDING);
        when(hireRepository.findById(100L)).thenReturn(Optional.of(hire));

        assertThatThrownBy(() -> hireService.updateStatus(
                100L, new HireStatusUpdateRequest(HireStatus.CANCELLED), principalFor(professionalUser)))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void updateStatus_unrelatedProfessionalTriesToAccept_throwsAccessDenied() {
        Hire hire = hireWithStatus(HireStatus.PENDING);
        when(hireRepository.findById(100L)).thenReturn(Optional.of(hire));

        User otherProfessional = User.builder().id(OTHER_USER_ID).email("otherpro@test.com").role(Role.PROFESSIONAL).active(true).build();

        assertThatThrownBy(() -> hireService.updateStatus(
                100L, new HireStatusUpdateRequest(HireStatus.ACCEPTED), principalFor(otherProfessional)))
                .isInstanceOf(AccessDeniedException.class);
    }

    // ---- getMine ----

    @Test
    void getMine_asClient_returnsClientHires() {
        Hire hire = hireWithStatus(HireStatus.PENDING);
        when(hireRepository.findByClientId(CLIENT_ID)).thenReturn(List.of(hire));

        List<HireResponse> result = hireService.getMine(principalFor(client));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).clientId()).isEqualTo(CLIENT_ID);
    }

    @Test
    void getMine_asProfessional_returnsProfessionalHires() {
        Hire hire = hireWithStatus(HireStatus.PENDING);
        when(professionalProfileRepository.findByUserId(PROFESSIONAL_USER_ID)).thenReturn(Optional.of(professionalProfile));
        when(hireRepository.findByProfessionalId(10L)).thenReturn(List.of(hire));

        List<HireResponse> result = hireService.getMine(principalFor(professionalUser));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).professionalId()).isEqualTo(10L);
    }

    @Test
    void getMine_asProfessionalWithoutProfile_returnsEmpty() {
        when(professionalProfileRepository.findByUserId(PROFESSIONAL_USER_ID)).thenReturn(Optional.empty());

        List<HireResponse> result = hireService.getMine(principalFor(professionalUser));

        assertThat(result).isEmpty();
    }
}
