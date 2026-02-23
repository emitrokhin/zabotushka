package ru.mitrohinayulya.zabotushka.authorization.application;

import ru.mitrohinayulya.zabotushka.shared.domain.Platform;

/// Command object (input DTO) for the authorization use case.
///
/// Replaces the two separate request DTOs {@code AuthorizeTelegramRequest} and
/// {@code AuthorizeMaxRequest} and the procedural
/// {@code GreenwayAuthorizationService#authorize(PlatformAuthorizationService, ...)} signature
/// that passed a service as a callback parameter.
public record AuthorizeUserCommand(long platformUserId, Platform platform, long greenwayId, String regDate) {}
