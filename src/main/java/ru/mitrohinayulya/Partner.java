package ru.mitrohinayulya;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;

@Entity
public class Partner extends PanacheEntity {
    public long telegramId;
    public boolean authorized;
    public String firstName;
    public String lastName;
    public long greenwayId;
    public Qualification rank;
    public Qualification qualification;
}
