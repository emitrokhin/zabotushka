package ru.mitrohinayulya.zabotushka;

import io.quarkus.test.junit.QuarkusTestProfile;
import java.util.Set;

/**
 * Профиль для тестов - отключает GreenwayService startup
 */
public class GreenwayServiceTestProfile implements QuarkusTestProfile {

    @Override
    public Set<Class<?>> getEnabledAlternatives() {
        // Отключаем @Startup для GreenwayService в тестах
        return Set.of();
    }

    @Override
    public String getConfigProfile() {
        return "test";
    }
}
