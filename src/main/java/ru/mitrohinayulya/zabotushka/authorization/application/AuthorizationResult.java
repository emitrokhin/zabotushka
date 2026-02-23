package ru.mitrohinayulya.zabotushka.authorization.application;

/// Result of the authorization use case.
///
/// Using a typed result instead of a raw {@code jakarta.ws.rs.core.Response} keeps
/// the application service free of HTTP concerns. The resource layer maps this to HTTP.
public sealed interface AuthorizationResult {

    record Authorized()             implements AuthorizationResult {}
    record AlreadyAuthorized()      implements AuthorizationResult {}
    record DataMismatch()           implements AuthorizationResult {}
    record GreenwayIdConflict()     implements AuthorizationResult {}
    record PartnerNotFound()        implements AuthorizationResult {}
    record InvalidCredentials()     implements AuthorizationResult {}
    record GreenwayUnavailable()    implements AuthorizationResult {}
}
