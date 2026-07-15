package com.marketplace.professional.catalog;

import com.marketplace.professional.catalog.dto.ProfessionalProfileRequest;
import com.marketplace.professional.catalog.dto.ProfessionalProfileResponse;
import com.marketplace.professional.domain.entity.ProfessionalProfile;
import com.marketplace.professional.domain.entity.User;
import com.marketplace.professional.domain.enums.Role;
import com.marketplace.professional.domain.repository.ProfessionalProfileRepository;
import com.marketplace.professional.domain.repository.UserRepository;
import com.marketplace.professional.exception.BadRequestException;
import com.marketplace.professional.exception.ResourceNotFoundException;
import com.marketplace.professional.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfessionalProfileService {

    private final ProfessionalProfileRepository professionalProfileRepository;
    private final UserRepository userRepository;

    @Transactional
    public ProfessionalProfileResponse create(Long userId, ProfessionalProfileRequest request) {
        professionalProfileRepository.findByUserId(userId).ifPresent(existing -> {
            throw new BadRequestException("A professional profile already exists for this user");
        });

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        ProfessionalProfile profile = ProfessionalProfile.builder()
                .user(user)
                .headline(request.headline())
                .bio(request.bio())
                .category(request.category())
                .location(request.location())
                .hourlyRate(request.hourlyRate())
                .build();

        ProfessionalProfile saved = professionalProfileRepository.save(profile);
        log.info("Created professional profile {} for user {}", saved.getId(), userId);
        return ProfessionalProfileResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<ProfessionalProfileResponse> list(String category) {
        return professionalProfileRepository.search(category).stream()
                .map(ProfessionalProfileResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProfessionalProfileResponse getById(Long id) {
        return ProfessionalProfileResponse.from(findEntity(id));
    }

    @Transactional
    public ProfessionalProfileResponse update(Long id, CustomUserDetails principal, ProfessionalProfileRequest request) {
        ProfessionalProfile profile = findEntity(id);
        assertOwnerOrAdmin(profile, principal);

        profile.setHeadline(request.headline());
        profile.setBio(request.bio());
        profile.setCategory(request.category());
        profile.setLocation(request.location());
        profile.setHourlyRate(request.hourlyRate());

        ProfessionalProfile saved = professionalProfileRepository.save(profile);
        log.info("Updated professional profile {}", id);
        return ProfessionalProfileResponse.from(saved);
    }

    private ProfessionalProfile findEntity(Long id) {
        return professionalProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Professional profile not found: " + id));
    }

    private void assertOwnerOrAdmin(ProfessionalProfile profile, CustomUserDetails principal) {
        boolean isAdmin = principal.getRole() == Role.ADMIN;
        boolean isOwner = profile.getUser().getId().equals(principal.getUserId());
        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("You do not have permission to modify this professional profile");
        }
    }
}
