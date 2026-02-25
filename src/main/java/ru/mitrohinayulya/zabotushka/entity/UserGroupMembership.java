package ru.mitrohinayulya.zabotushka.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/// Entity for storing user group membership information
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
    public long platformUserId;

    @Column(name = "chat_id", nullable = false)
    public long chatId;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform", nullable = false)
    public Platform platform;

    @Column(name = "joined_at", nullable = false)
    public LocalDateTime joinedAt;

    @Column(name = "last_checked_at")
    public LocalDateTime lastCheckedAt;

    /// Finds all members of a specific group for the given platform
    public static List<UserGroupMembership> findByChatIdAndPlatform(long chatId, Platform platform) {
        return list("chatId = ?1 and platform = ?2", chatId, platform);
    }

    /// Checks if a membership record exists
    public static boolean exists(long platformUserId, long chatId, Platform platform) {
        return count(QUERY_BY_PLATFORM_USER_AND_CHAT_AND_PLATFORM, platformUserId, chatId, platform) > 0;
    }

    /// Removes a membership record
    public static boolean removeMembership(long platformUserId, long chatId, Platform platform) {
        return delete(QUERY_BY_PLATFORM_USER_AND_CHAT_AND_PLATFORM, platformUserId, chatId, platform) > 0;
    }
}
