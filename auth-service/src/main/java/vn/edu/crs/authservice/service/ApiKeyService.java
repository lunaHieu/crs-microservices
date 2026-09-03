package vn.edu.crs.authservice.service;

import vn.edu.crs.authservice.dto.ApiKeyCreateRequestDTO;
import vn.edu.crs.authservice.dto.ApiKeyResponseDTO;
import vn.edu.crs.authservice.entity.ApiKey;
import vn.edu.crs.authservice.repository.ApiKeyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ApiKeyService {

    private static final String ACTIVE = "ACTIVE";
    private static final String REVOKED = "REVOKED";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ApiKeyRepository apiKeyRepository;

    public ApiKeyResponseDTO create(ApiKeyCreateRequestDTO dto) {
        ApiKey apiKey = new ApiKey();
        apiKey.setKeyValue(generateRandomKey());
        apiKey.setOwnerName(dto.getOwnerName());
        apiKey.setScopes(dto.getScopes());
        apiKey.setStatus(ACTIVE);
        apiKey.setCreatedAt(LocalDateTime.now());

        if (dto.getValidDays() != null) {
            apiKey.setExpiresAt(LocalDateTime.now().plusDays(dto.getValidDays()));
        }

        return toDTO(apiKeyRepository.save(apiKey));
    }

    public List<ApiKeyResponseDTO> getAll() {
        return apiKeyRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public void revoke(Long id) {
        ApiKey apiKey = apiKeyRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        "Khong tim thay API Key id = " + id
                ));

        apiKey.setStatus(REVOKED);
        apiKeyRepository.save(apiKey);
    }

    public boolean isValidForScope(String keyValue, String requiredScope) {
        return apiKeyRepository.findByKeyValue(keyValue)
                .filter(key -> ACTIVE.equals(key.getStatus()))
                .filter(key -> key.getExpiresAt() == null
                        || key.getExpiresAt().isAfter(LocalDateTime.now()))
                .filter(key -> List.of(key.getScopes().split(","))
                        .contains(requiredScope))
                .isPresent();
    }

    private String generateRandomKey() {
        byte[] bytes = new byte[24];
        RANDOM.nextBytes(bytes);

        return "crs_" + Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    private ApiKeyResponseDTO toDTO(ApiKey key) {
        return new ApiKeyResponseDTO(
                key.getId(),
                key.getKeyValue(),
                key.getOwnerName(),
                key.getScopes(),
                key.getStatus(),
                key.getExpiresAt(),
                key.getCreatedAt()
        );
    }
}