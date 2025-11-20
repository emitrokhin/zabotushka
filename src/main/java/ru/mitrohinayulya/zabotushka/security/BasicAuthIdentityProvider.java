package ru.mitrohinayulya.zabotushka.security;

import io.quarkus.security.AuthenticationFailedException;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.IdentityProvider;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.request.UsernamePasswordAuthenticationRequest;
import io.quarkus.security.runtime.QuarkusPrincipal;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mitrohinayulya.zabotushka.config.BasicAuthConfig;

/**
 * Custom Identity Provider для Basic Authentication
 */
@ApplicationScoped
public class BasicAuthIdentityProvider implements IdentityProvider<UsernamePasswordAuthenticationRequest> {

    private static final Logger log = LoggerFactory.getLogger(BasicAuthIdentityProvider.class);

    @Inject
    BasicAuthConfig authConfig;

    @Override
    public Class<UsernamePasswordAuthenticationRequest> getRequestType() {
        return UsernamePasswordAuthenticationRequest.class;
    }

    @Override
    public Uni<SecurityIdentity> authenticate(UsernamePasswordAuthenticationRequest request,
                                               AuthenticationRequestContext context) {
        String username = request.getUsername();
        String password = new String(request.getPassword().getPassword());

        log.debug("Attempting authentication for user: {}", username);

        // Проверяем учетные данные
        if (authConfig.username().equals(username) && authConfig.password().equals(password)) {
            log.info("User authenticated successfully: {}", username);
            return Uni.createFrom().item(QuarkusSecurityIdentity.builder()
                    .setPrincipal(new QuarkusPrincipal(username))
                    .addRole("user")
                    .build());
        }

        log.warn("Authentication failed for user: {}", username);
        return Uni.createFrom().failure(new AuthenticationFailedException("Invalid credentials"));
    }
}
