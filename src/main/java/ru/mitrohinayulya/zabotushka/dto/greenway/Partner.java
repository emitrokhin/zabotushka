package ru.mitrohinayulya.zabotushka.dto.greenway;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Данные партнера из MyGreenway API
 */
public record Partner(
    @JsonProperty("id")
    Integer id,

    @JsonProperty("last_name")
    String lastName,

    @JsonProperty("first_name")
    String firstName,

    @JsonProperty("pat_name")
    String patronymic,

    @JsonProperty("birthday")
    String birthday,

    @JsonProperty("email")
    String email,

    @JsonProperty("phone")
    String phone,

    @JsonProperty("number")
    Integer number,

    @JsonProperty("agreement_state")
    String agreementState,

    @JsonProperty("reg_date")
    String regDate,

    @JsonProperty("country_name")
    String countryName,

    @JsonProperty("city_id")
    Integer cityId,

    @JsonProperty("city_name")
    String cityName,

    @JsonProperty("vk")
    Object vk,

    @JsonProperty("telegram")
    Object telegram,

    @JsonProperty("instagram")
    Object instagram,

    @JsonProperty("whatsapp")
    Object whatsapp,

    @JsonProperty("lo")
    Double lo,

    @JsonProperty("lgo")
    Double lgo,

    @JsonProperty("sgo")
    Double sgo,

    @JsonProperty("qual")
    String qualification,

    @JsonProperty("level")
    Integer level,

    @JsonProperty("has_children")
    Boolean hasChildren,

    @JsonProperty("mentor_full_name")
    String mentorFullName
) {
}
