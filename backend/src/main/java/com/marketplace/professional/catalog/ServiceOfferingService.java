package com.marketplace.professional.catalog;

import com.marketplace.professional.catalog.dto.ServiceOfferingRequest;
import com.marketplace.professional.catalog.dto.ServiceOfferingResponse;
import com.marketplace.professional.domain.entity.ProfessionalProfile;
import com.marketplace.professional.domain.entity.ServiceOffering;
import com.marketplace.professional.domain.enums.Role;
import com.marketplace.professional.domain.repository.ProfessionalProfileRepository;
import com.marketplace.professional.domain.repository.ServiceOfferingRepository;
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
public class ServiceOfferingService {

    private final ServiceOfferingRepository serviceOfferingRepository;
    private final ProfessionalProfileRepository professionalProfileRepository;

    @Transactional
    public ServiceOfferingResponse create(Long userId, ServiceOfferingRequest request) {
        ProfessionalProfile profile = professionalProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new BadRequestException(
                        "You must create a professional profile before adding service offerings"));

        ServiceOffering offering = ServiceOffering.builder()
                .professional(profile)
                .title(request.title())
                .description(request.description())
                .category(request.category())
                .price(request.price())
                .build();

        ServiceOffering saved = serviceOfferingRepository.save(offering);
        log.info("Created service offering {} for professional {}", saved.getId(), profile.getId());
        return ServiceOfferingResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<ServiceOfferingResponse> list(String category) {
        List<ServiceOffering> offerings = (category == null || category.isBlank())
                ? serviceOfferingRepository.findByActiveTrue()
                : serviceOfferingRepository.findByCategoryAndActiveTrue(category);
        return offerings.stream().map(ServiceOfferingResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ServiceOfferingResponse getById(Long id) {
        return ServiceOfferingResponse.from(findEntity(id));
    }

    @Transactional
    public ServiceOfferingResponse update(Long id, CustomUserDetails principal, ServiceOfferingRequest request) {
        ServiceOffering offering = findEntity(id);
        assertOwnerOrAdmin(offering, principal);

        offering.setTitle(request.title());
        offering.setDescription(request.description());
        offering.setCategory(request.category());
        offering.setPrice(request.price());

        ServiceOffering saved = serviceOfferingRepository.save(offering);
        log.info("Updated service offering {}", id);
        return ServiceOfferingResponse.from(saved);
    }

    @Transactional
    public void delete(Long id, CustomUserDetails principal) {
        ServiceOffering offering = findEntity(id);
        assertOwnerOrAdmin(offering, principal);
        offering.setActive(false);
        serviceOfferingRepository.save(offering);
        log.info("Soft-deleted service offering {}", id);
    }

    private ServiceOffering findEntity(Long id) {
        return serviceOfferingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service offering not found: " + id));
    }

    private void assertOwnerOrAdmin(ServiceOffering offering, CustomUserDetails principal) {
        boolean isAdmin = principal.getRole() == Role.ADMIN;
        boolean isOwner = offering.getProfessional().getUser().getId().equals(principal.getUserId());
        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("You do not have permission to modify this service offering");
        }
    }
}
