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
       uniqueConstraints = @UniqueConstraint(columnNames = {"telegram_id", "chat_id"}))
public class UserGroupMembership extends PanacheEntityBase {

    private static final String QUERY_BY_TELEGRAM_AND_CHAT = "telegramId = ?1 and chatId = ?2";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false)
    public UUID id;

    @Column(name = "telegram_id", nullable = false)
    public Long telegramId;

    @Column(name = "chat_id", nullable = false)
    public Long chatId;

    @Column(name = "joined_at", nullable = false)
    public LocalDateTime joinedAt;

    @Column(name = "last_checked_at")
    public LocalDateTime lastCheckedAt;

    /**
     * Поиск всех членов конкретной группы
     */
    public static List<UserGroupMembership> findByChatId(Long chatId) {
        return list("chatId", chatId);
    }

    /**
     * Проверка существования записи о членстве
     */
    public static boolean exists(Long telegramId, Long chatId) {
        return count(QUERY_BY_TELEGRAM_AND_CHAT, telegramId, chatId) > 0;
    }

    /**
     * Удаление записи о членстве
     */
    public static boolean removeMembership(Long telegramId, Long chatId) {
        return delete(QUERY_BY_TELEGRAM_AND_CHAT, telegramId, chatId) > 0;
    }
}
