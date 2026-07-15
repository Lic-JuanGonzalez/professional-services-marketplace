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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Encapsulates the hiring workflow: creation, ownership checks and the status transition
 * matrix (PENDING -> ACCEPTED/REJECTED/CANCELLED, ACCEPTED -> COMPLETED).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HireService {

    private final HireRepository hireRepository;
    private final ServiceOfferingRepository serviceOfferingRepository;
    private final ProfessionalProfileRepository professionalProfileRepository;
    private final UserRepository userRepository;

    public HireResponse createHire(CustomUserDetails principal, HireRequest request) {
        if (principal.getRole() != Role.CLIENT) {
            throw new AccessDeniedException("Only clients can create a hire request");
        }

        ServiceOffering offering = serviceOfferingRepository.findById(request.serviceOfferingId())
                .filter(o -> Boolean.TRUE.equals(o.getActive()))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Service offering not found: " + request.serviceOfferingId()));

        User client = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + principal.getUserId()));

        Hire hire = Hire.builder()
                .client(client)
                .professional(offering.getProfessional())
                .serviceOffering(offering)
                .status(HireStatus.PENDING)
                .scheduledDate(request.scheduledDate())
                .notes(request.notes())
                .build();

        Hire saved = hireRepository.save(hire);
        log.info("Created hire {} for client {} on offering {}", saved.getId(), client.getId(), offering.getId());
        return toResponse(saved);
    }

    public List<HireResponse> getMine(CustomUserDetails principal) {
        List<Hire> hires = switch (principal.getRole()) {
            case CLIENT -> hireRepository.findByClientId(principal.getUserId());
            case PROFESSIONAL -> professionalProfileRepository.findByUserId(principal.getUserId())
                    .map(ProfessionalProfile::getId)
                    .map(hireRepository::findByProfessionalId)
                    .orElseGet(List::of);
            case ADMIN -> List.of();
        };
        return hires.stream().map(this::toResponse).toList();
    }

    public HireResponse getById(Long id, CustomUserDetails principal) {
        Hire hire = findHireOrThrow(id);
        if (!canView(hire, principal)) {
            throw new AccessDeniedException("You are not allowed to view this hire");
        }
        return toResponse(hire);
    }

    public HireResponse updateStatus(Long id, HireStatusUpdateRequest request, CustomUserDetails principal) {
        Hire hire = findHireOrThrow(id);
        applyTransition(hire, request.status(), principal);
        Hire saved = hireRepository.save(hire);
        log.info("Hire {} transitioned to {} by user {}", saved.getId(), saved.getStatus(), principal.getUserId());
        return toResponse(saved);
    }

    private Hire findHireOrThrow(Long id) {
        return hireRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hire not found: " + id));
    }

    private boolean canView(Hire hire, CustomUserDetails principal) {
        if (principal.getRole() == Role.ADMIN) {
            return true;
        }
        return isClient(hire, principal) || isProfessional(hire, principal);
    }

    private void applyTransition(Hire hire, HireStatus target, CustomUserDetails principal) {
        HireStatus current = hire.getStatus();

        boolean validTransition = switch (current) {
            case PENDING -> target == HireStatus.ACCEPTED
                    || target == HireStatus.REJECTED
                    || target == HireStatus.CANCELLED;
            case ACCEPTED -> target == HireStatus.COMPLETED;
            case REJECTED, COMPLETED, CANCELLED -> false;
        };

        if (!validTransition) {
            throw new BadRequestException(
                    "Cannot transition hire from %s to %s".formatted(current, target));
        }

        boolean actorAllowed = switch (target) {
            case ACCEPTED, REJECTED, COMPLETED ->
                    isProfessional(hire, principal) || principal.getRole() == Role.ADMIN;
            case CANCELLED -> isClient(hire, principal) || principal.getRole() == Role.ADMIN;
            case PENDING -> false;
        };

        if (!actorAllowed) {
            throw new AccessDeniedException(
                    "You are not allowed to transition this hire from %s to %s".formatted(current, target));
        }

        hire.setStatus(target);
    }

    private boolean isClient(Hire hire, CustomUserDetails principal) {
        return hire.getClient().getId().equals(principal.getUserId());
    }

    private boolean isProfessional(Hire hire, CustomUserDetails principal) {
        ProfessionalProfile professional = hire.getProfessional();
        return professional != null
                && professional.getUser() != null
                && professional.getUser().getId().equals(principal.getUserId());
    }

    private HireResponse toResponse(Hire hire) {
        return new HireResponse(
                hire.getId(),
                hire.getClient().getId(),
                hire.getProfessional().getId(),
                hire.getServiceOffering().getId(),
                hire.getServiceOffering().getTitle(),
                hire.getStatus(),
                hire.getScheduledDate(),
                hire.getNotes(),
                hire.getCreatedAt()
        );
    }
}
