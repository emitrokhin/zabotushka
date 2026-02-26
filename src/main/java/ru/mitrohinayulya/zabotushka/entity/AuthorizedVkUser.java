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

/// Entity for storing authorized VK users
@Entity
@Table(name = "authorized_vk_users",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_vk_id", columnNames = "vk_id")
       })
public class AuthorizedVkUser extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false)
    public UUID id;

    @Column(name = "vk_id", nullable = false, unique = true)
    public long vkId;

    @Column(name = "greenway_id", nullable = false)
    public long greenwayId;

    @Column(name = "reg_date", nullable = false)
    public String regDate;

    @Column(name = "creation_date", nullable = false)
    public LocalDateTime creationDate;

    /// Finds a user by vkId
    public static AuthorizedVkUser findByVkId(long vkId) {
        return find("vkId", vkId).firstResult();
    }

    /// Checks if a user exists by vkId
    public static boolean existsByVkId(long vkId) {
        return count("vkId", vkId) > 0;
    }

    /// Checks if a user exists by greenwayId
    public static boolean existsByGreenwayId(long greenwayId) {
        return count("greenwayId", greenwayId) > 0;
    }
}
