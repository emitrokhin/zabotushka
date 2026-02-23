package ru.mitrohinayulya.zabotushka.qualification.domain;

import java.util.Optional;

/// Port: minimal Greenway API surface needed by the Authorization bounded context.
///
/// The Authorization context needs only one thing from Greenway: the registration date
/// of a partner, to verify the user-supplied date. It should not depend on the full
/// {@code GreenwayPartnerService} infrastructure class.
///
/// Implementation lives in {@code qualification.infrastructure.GreenwayPartnerAdapter}.
public interface GreenwayPartnerPort {

    /// Returns the registration date string as stored in Greenway for the given partner ID,
    /// or empty if the partner is not found.
    Optional<String> findRegistrationDate(long greenwayId);
}
