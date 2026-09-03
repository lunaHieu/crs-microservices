package vn.edu.crs.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ApiKeyResponseDTO {

    private Long id;
    private String keyValue;
    private String ownerName;
    private String scopes;
    private String status;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
}