package ru.mitrohinayulya.zabotushka.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;
import java.util.UUID;

/// Entity для хранения авторизованных Telegram пользователей
@Entity
@Table(name = "authorized_telegram_users",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_telegram_id", columnNames = "telegram_id")
       })
public class AuthorizedTelegramUser extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false)
    public UUID id;

    @Column(name = "telegram_id", nullable = false, unique = true)
    public long telegramId;

    @Column(name = "greenway_id", nullable = false)
    public long greenwayId;

    @Column(name = "reg_date", nullable = false)
    public String regDate;

    @Column(name = "creation_date", nullable = false)
    public LocalDateTime creationDate;

    /// Поиск пользователя по telegramId
    public static AuthorizedTelegramUser findByTelegramId(long telegramId) {
        return find("telegramId", telegramId).firstResult();
    }

    /// Проверка существования пользователя по telegramId
    public static boolean existsByTelegramId(long telegramId) {
        return count("telegramId", telegramId) > 0;
    }

    /// Проверка существования пользователя по greenwayId
    public static boolean existsByGreenwayId(long greenwayId) {
        return count("greenwayId", greenwayId) > 0;
    }
}
