package ru.mitrohinayulya.zabotushka.dto.greenway;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/// Ответ на запрос списка партнеров
/// @param authPartner Авторизованный партнер (от имени которого выполняется запрос)
/// @param partners Список найденных партнеров
/// @param code Код ошибки (если есть)
/// @param detail Детали ошибки (если есть)
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
