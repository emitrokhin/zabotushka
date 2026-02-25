package ru.mitrohinayulya.zabotushka.dto.greenway;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/// Response for a partner list request
/// @param authPartner Authorized partner (on whose behalf the request is made)
/// @param partners List of found partners
/// @param code Error code (if any)
/// @param detail Error details (if any)
public record PartnerListResponse(
    @JsonProperty("auth_partner")
    Partner authPartner,

    @JsonProperty("partners")
    List<Partner> partners,

    @JsonProperty("code")
    String code,

    @JsonProperty("detail")
    String detail
) { }
