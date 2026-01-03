package ru.mitrohinayulya.zabotushka.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity для хранения авторизованных пользователей
 */
@Entity
@Table(name = "authorized_users",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_telegram_id", columnNames = "telegram_id")
       })
public class AuthorizedUser extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false)
    public UUID id;

    @Column(name = "telegram_id", nullable = false, unique = true)
    public Long telegramId;

    @Column(name = "greenway_id", nullable = false)
    public Long greenwayId;

    @Column(name = "reg_date", nullable = false)
    public String regDate;

    @Column(name = "creation_date", nullable = false)
    public LocalDateTime creationDate;

    /**
     * Поиск пользователя по telegramId
     */
    public static AuthorizedUser findByTelegramId(Long telegramId) {
        return find("telegramId", telegramId).firstResult();
    }

    /**
     * Проверка существования пользователя по telegramId
     */
    public static boolean existsByTelegramId(Long telegramId) {
        return count("telegramId", telegramId) > 0;
    }

    /**
     * Проверка существования пользователя по greenwayId
     */
    public static boolean existsByGreenwayId(Long greenwayId) {
        return count("greenwayId", greenwayId) > 0;
    }
}
