package ru.mitrohinayulya.zabotushka;

import io.quarkus.test.junit.QuarkusTestProfile;
import java.util.Set;

/// Test profile that disables GreenwayPartnerService startup
public class GreenwayServiceTestProfile implements QuarkusTestProfile {

    @Override
    public Set<Class<?>> getEnabledAlternatives() {
        // Disable @Startup for GreenwayPartnerService in tests
        return Set.of();
    }

    @Override
    public String getConfigProfile() {
        return "test";
    }
}
