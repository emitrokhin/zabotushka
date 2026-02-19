package ru.mitrohinayulya.zabotushka.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Entity для хранения информации о членстве пользователей в группах
 */
@Entity
@Table(name = "user_group_memberships",
       uniqueConstraints = @UniqueConstraint(columnNames = {"platform_user_id", "chat_id", "platform"}))
public class UserGroupMembership extends PanacheEntityBase {

    private static final String QUERY_BY_PLATFORM_USER_AND_CHAT_AND_PLATFORM =
            "platformUserId = ?1 and chatId = ?2 and platform = ?3";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false)
    public UUID id;

    @Column(name = "platform_user_id", nullable = false)
    public Long platformUserId;

    @Column(name = "chat_id", nullable = false)
    public Long chatId;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform", nullable = false)
    public Platform platform;

    @Column(name = "joined_at", nullable = false)
    public LocalDateTime joinedAt;

    @Column(name = "last_checked_at")
    public LocalDateTime lastCheckedAt;

    /**
     * Поиск всех членов конкретной группы для указанной платформы
     */
    public static List<UserGroupMembership> findByChatIdAndPlatform(Long chatId, Platform platform) {
        return list("chatId = ?1 and platform = ?2", chatId, platform);
    }

    /**
     * Проверка существования записи о членстве
     */
    public static boolean exists(Long platformUserId, Long chatId, Platform platform) {
        return count(QUERY_BY_PLATFORM_USER_AND_CHAT_AND_PLATFORM, platformUserId, chatId, platform) > 0;
    }

    /**
     * Удаление записи о членстве
     */
    public static boolean removeMembership(Long platformUserId, Long chatId, Platform platform) {
        return delete(QUERY_BY_PLATFORM_USER_AND_CHAT_AND_PLATFORM, platformUserId, chatId, platform) > 0;
    }
}
