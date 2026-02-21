package ru.mitrohinayulya.zabotushka.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity для хранения авторизованных Max пользователей
 */
@Entity
@Table(name = "authorized_max_users",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_max_id", columnNames = "max_id")
       })
public class AuthorizedMaxUser extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false)
    public UUID id;

    @Column(name = "max_id", nullable = false, unique = true)
    public long maxId;

    @Column(name = "greenway_id", nullable = false)
    public long greenwayId;

    @Column(name = "reg_date", nullable = false)
    public String regDate;

    @Column(name = "creation_date", nullable = false)
    public LocalDateTime creationDate;

    /**
     * Поиск пользователя по maxId
     */
    public static AuthorizedMaxUser findByMaxId(long maxId) {
        return find("maxId", maxId).firstResult();
    }

    /**
     * Проверка существования пользователя по maxId
     */
    public static boolean existsByMaxId(long maxId) {
        return count("maxId", maxId) > 0;
    }

    /**
     * Проверка существования пользователя по greenwayId
     */
    public static boolean existsByGreenwayId(long greenwayId) {
        return count("greenwayId", greenwayId) > 0;
    }
}
