package ru.mitrohinayulya.zabotushka.service;

/**
 * Интерфейс для платформо-зависимых операций с пользователями
 */
public interface PlatformAuthorizationService {
    boolean existsByPlatformId(Long platformId);
    boolean matchesStoredData(Long platformId, Long greenwayId, String regDate);
    void saveUser(Long platformId, Long greenwayId, String regDate);
    boolean existsByGreenwayId(Long greenwayId);
}
