package ru.mitrohinayulya.zabotushka.service.platform;

/// Interface for platform-specific user operations
public interface PlatformAuthorizationService {
    boolean existsByPlatformId(long platformId);
    boolean matchesStoredData(long platformId, long greenwayId, String regDate);
    void saveUser(long platformId, long greenwayId, String regDate);
    boolean existsByGreenwayId(long greenwayId);
}
