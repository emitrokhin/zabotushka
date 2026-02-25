package ru.mitrohinayulya.zabotushka.dto.greenway;

import com.fasterxml.jackson.annotation.JsonProperty;

/// Response to a MyGreenway partner authorization request
/// @param authorized Authorization status: "authorized" if registration date matches, "not_authorized" otherwise
public record AuthorizeResponse(
    @JsonProperty("authorized")
    String authorized
) {
    /// Creates a successful authorization response
    public static AuthorizeResponse createAuthorized() {
        return new AuthorizeResponse("authorized");
    }

    /// Creates an unsuccessful authorization response
    public static AuthorizeResponse createNotAuthorized() {
        return new AuthorizeResponse("not_authorized");
    }
}
