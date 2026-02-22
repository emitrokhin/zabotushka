package ru.mitrohinayulya.zabotushka.resource;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.mitrohinayulya.zabotushka.GreenwayServiceTestProfile;
import ru.mitrohinayulya.zabotushka.dto.greenway.Partner;
import ru.mitrohinayulya.zabotushka.dto.greenway.PartnerListResponse;
import ru.mitrohinayulya.zabotushka.exception.GreenwayApiException;
import ru.mitrohinayulya.zabotushka.service.greenway.GreenwayPartnerService;
import ru.mitrohinayulya.zabotushka.service.telegram.AuthorizedTelegramUserService;

import java.util.List;
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@QuarkusTest
@TestProfile(GreenwayServiceTestProfile.class)
class TelegramAuthorizationResourceTest {

    @InjectMock
    GreenwayPartnerService greenwayPartnerService;

    @InjectMock
    AuthorizedTelegramUserService authorizedTelegramUserService;

    @Test
    @DisplayName("authorize returns 200 and saves user on first-time authorization")
    void authorize_ShouldReturn200AndSaveUser_WhenFirstTimeAuthorization() {
        var partner = createPartner(123456);
        var partnerListResponse = new PartnerListResponse(null, List.of(partner), null, null);

        when(authorizedTelegramUserService.existsByPlatformId(1001L)).thenReturn(false);
        when(greenwayPartnerService.getPartnerList(anyLong(), anyInt())).thenReturn(partnerListResponse);
        when(greenwayPartnerService.findPartnerById(any(PartnerListResponse.class), anyLong())).thenReturn(Optional.of(partner));

        given()
            .auth().basic("admin", "admin")
            .contentType(ContentType.JSON)
            .body("""
                {
                    "telegramId": 1001,
                    "greenwayId": 123456,
                    "regDate": "15.01.2023"
                }
                """)
            .when()
            .post("/greenway/authorize/telegram")
            .then()
            .statusCode(200)
            .body("authorized", is("authorized"));

        verify(authorizedTelegramUserService, times(1)).saveUser(1001L, 123456L, "15.01.2023");
    }

    @Test
    @DisplayName("authorize returns 200 without calling API on re-authorization with matching data")
    void authorize_ShouldReturn200WithoutApiCall_WhenReauthorizationWithMatchingData() {
        when(authorizedTelegramUserService.existsByPlatformId(1002L)).thenReturn(true);
        when(authorizedTelegramUserService.matchesStoredData(1002L, 123456L, "15.01.2023")).thenReturn(true);

        given()
            .auth().basic("admin", "admin")
            .contentType(ContentType.JSON)
            .body("""
                {
                    "telegramId": 1002,
                    "greenwayId": 123456,
                    "regDate": "15.01.2023"
                }
                """)
            .when()
            .post("/greenway/authorize/telegram")
            .then()
            .statusCode(200)
            .body("authorized", is("authorized"));

        verify(greenwayPartnerService, never()).getPartnerList(anyLong(), anyInt());
        verify(authorizedTelegramUserService, never()).saveUser(anyLong(), anyLong(), anyString());
    }

    @Test
    @DisplayName("authorize returns 403 when re-authorization data does not match stored credentials")
    void authorize_ShouldReturn403_WhenReauthorizationDataMismatch() {
        when(authorizedTelegramUserService.existsByPlatformId(1003L)).thenReturn(true);
        when(authorizedTelegramUserService.matchesStoredData(1003L, 123456L, "20.01.2023")).thenReturn(false);

        given()
            .auth().basic("admin", "admin")
            .contentType(ContentType.JSON)
            .body("""
                {
                    "telegramId": 1003,
                    "greenwayId": 123456,
                    "regDate": "20.01.2023"
                }
                """)
            .when()
            .post("/greenway/authorize/telegram")
            .then()
            .statusCode(403)
            .body("error", is("Authorization data does not match stored credentials"));

        verify(greenwayPartnerService, never()).getPartnerList(anyLong(), anyInt());
    }

    @Test
    @DisplayName("authorize returns 409 when Greenway ID is already associated with another account")
    void authorize_ShouldReturn409_WhenGreenwayIdAlreadyUsed() {
        when(authorizedTelegramUserService.existsByPlatformId(9999L)).thenReturn(false);
        when(authorizedTelegramUserService.existsByGreenwayId(123456L)).thenReturn(true);

        given()
            .auth().basic("admin", "admin")
            .contentType(ContentType.JSON)
            .body("""
                {
                    "telegramId": 9999,
                    "greenwayId": 123456,
                    "regDate": "15.01.2023"
                }
                """)
            .when()
            .post("/greenway/authorize/telegram")
            .then()
            .statusCode(409)
            .body("error", is("This Greenway ID is already associated with another account"));

        verify(greenwayPartnerService, never()).getPartnerList(anyLong(), anyInt());
        verify(authorizedTelegramUserService, never()).saveUser(anyLong(), anyLong(), anyString());
    }

    @Test
    @DisplayName("authorize returns 401 when no Basic Auth credentials provided")
    void authorize_ShouldReturn401_WhenNoBasicAuth() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "telegramId": 1004,
                    "greenwayId": 123456,
                    "regDate": "15.01.2023"
                }
                """)
            .when()
            .post("/greenway/authorize/telegram")
            .then()
            .statusCode(401);
    }

    @Test
    @DisplayName("authorize returns 401 when wrong Basic Auth credentials provided")
    void authorize_ShouldReturn401_WhenWrongCredentials() {
        given()
            .auth().basic("wrong", "wrong")
            .contentType(ContentType.JSON)
            .body("""
                {
                    "telegramId": 1005,
                    "greenwayId": 123456,
                    "regDate": "15.01.2023"
                }
                """)
            .when()
            .post("/greenway/authorize/telegram")
            .then()
            .statusCode(401);
    }

    @Test
    @DisplayName("authorize returns 401 when registration date does not match Greenway data")
    void authorize_ShouldReturn401_WhenRegistrationDateMismatch() {
        var partner = createPartner(123456);
        var response = new PartnerListResponse(null, List.of(partner), null, null);

        when(authorizedTelegramUserService.existsByPlatformId(1006L)).thenReturn(false);
        when(greenwayPartnerService.getPartnerList(anyLong(), anyInt())).thenReturn(response);
        when(greenwayPartnerService.findPartnerById(any(PartnerListResponse.class), anyLong())).thenReturn(Optional.of(partner));

        given()
            .auth().basic("admin", "admin")
            .contentType(ContentType.JSON)
            .body("""
                {
                    "telegramId": 1006,
                    "greenwayId": 123456,
                    "regDate": "20.01.2023"
                }
                """)
            .when()
            .post("/greenway/authorize/telegram")
            .then()
            .statusCode(401)
            .body("authorized", is("not_authorized"));
    }

    @Test
    @DisplayName("authorize returns 404 when partner ID is not found in Greenway response")
    void authorize_ShouldReturn404_WhenPartnerNotFound() {
        var otherPartner = createPartner(999999);
        var response = new PartnerListResponse(null, List.of(otherPartner), null, null);

        when(authorizedTelegramUserService.existsByPlatformId(1007L)).thenReturn(false);
        when(greenwayPartnerService.getPartnerList(anyLong(), anyInt())).thenReturn(response);

        given()
            .auth().basic("admin", "admin")
            .contentType(ContentType.JSON)
            .body("""
                {
                    "telegramId": 1007,
                    "greenwayId": 123456,
                    "regDate": "15.01.2023"
                }
                """)
            .when()
            .post("/greenway/authorize/telegram")
            .then()
            .statusCode(404)
            .body("authorized", is("not_authorized"));
    }

    @Test
    @DisplayName("authorize returns 404 when partner list from Greenway is empty")
    void authorize_ShouldReturn404_WhenPartnerListIsEmpty() {
        var response = new PartnerListResponse(null, List.of(), null, null);

        when(authorizedTelegramUserService.existsByPlatformId(1008L)).thenReturn(false);
        when(greenwayPartnerService.getPartnerList(anyLong(), anyInt())).thenReturn(response);

        given()
            .auth().basic("admin", "admin")
            .contentType(ContentType.JSON)
            .body("""
                {
                    "telegramId": 1008,
                    "greenwayId": 123456,
                    "regDate": "15.01.2023"
                }
                """)
            .when()
            .post("/greenway/authorize/telegram")
            .then()
            .statusCode(404)
            .body("authorized", is("not_authorized"));
    }

    @Test
    @DisplayName("authorize returns 500 when Greenway API throws an exception")
    void authorize_ShouldReturn500_WhenGreenwayApiThrowsException() {
        when(authorizedTelegramUserService.existsByPlatformId(1009L)).thenReturn(false);
        when(greenwayPartnerService.getPartnerList(anyLong(), anyInt()))
            .thenThrow(new GreenwayApiException("API Error", "ERROR_CODE", "Error details"));

        given()
            .auth().basic("admin", "admin")
            .contentType(ContentType.JSON)
            .body("""
                {
                    "telegramId": 1009,
                    "greenwayId": 123456,
                    "regDate": "15.01.2023"
                }
                """)
            .when()
            .post("/greenway/authorize/telegram")
            .then()
            .statusCode(500)
            .body("authorized", is("not_authorized"));
    }

    private Partner createPartner(int number) {
        return new Partner(
            number, "Иванов", "Иван", "Иванович",
            "01.01.1990", "test@example.com", "+79001234567",
            number, "ACTIVE", "15.01.2023",
            "Россия", 1, "Москва",
            null, null, null, null,
            0.0, 0.0, 0.0, "NO", 1, false, "Петров Петр"
        );
    }
}
