package ru.mitrohinayulya.zabotushka.resource;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import ru.mitrohinayulya.zabotushka.dto.greenway.Partner;
import ru.mitrohinayulya.zabotushka.dto.greenway.PartnerListResponse;
import ru.mitrohinayulya.zabotushka.exception.GreenwayApiException;
import ru.mitrohinayulya.zabotushka.service.GreenwayService;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * Тесты для GreenwayResource
 */
@QuarkusTest
@io.quarkus.test.junit.TestProfile(ru.mitrohinayulya.zabotushka.TestProfile.class)
class GreenwayResourceTest {

    @InjectMock
    GreenwayService greenwayService;

    @Test
    void testAuthorize_Success() {
        // Given: партнер существует и дата регистрации совпадает
        var partner = createPartner(123456, "2023-01-15");
        var response = new PartnerListResponse(null, List.of(partner), null, null);

        when(greenwayService.getPartnerList(anyLong(), anyInt())).thenReturn(response);

        // When & Then: авторизация успешна
        given()
            .auth().basic("admin", "admin")
            .contentType(ContentType.JSON)
            .body("""
                {
                    "greenwayId": 123456,
                    "regDate": "2023-01-15"
                }
                """)
            .when()
            .post("/greenway/authorize")
            .then()
            .statusCode(200)
            .body("authorized", is("authorized"));
    }

    @Test
    void testAuthorize_Unauthorized_NoBasicAuth() {
        // When & Then: без Basic Auth доступ запрещен
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "greenwayId": 123456,
                    "regDate": "2023-01-15"
                }
                """)
            .when()
            .post("/greenway/authorize")
            .then()
            .statusCode(401);
    }

    @Test
    void testAuthorize_Unauthorized_WrongCredentials() {
        // When & Then: с неправильными учетными данными доступ запрещен
        given()
            .auth().basic("wrong", "wrong")
            .contentType(ContentType.JSON)
            .body("""
                {
                    "greenwayId": 123456,
                    "regDate": "2023-01-15"
                }
                """)
            .when()
            .post("/greenway/authorize")
            .then()
            .statusCode(401);
    }

    @Test
    void testAuthorize_DateMismatch() {
        // Given: партнер существует, но дата регистрации не совпадает
        var partner = createPartner(123456, "2023-01-15");
        var response = new PartnerListResponse(null, List.of(partner), null, null);

        when(greenwayService.getPartnerList(anyLong(), anyInt())).thenReturn(response);

        // When & Then: авторизация не удалась (неправильная дата)
        given()
            .auth().basic("admin", "admin")
            .contentType(ContentType.JSON)
            .body("""
                {
                    "greenwayId": 123456,
                    "regDate": "2023-01-20"
                }
                """)
            .when()
            .post("/greenway/authorize")
            .then()
            .statusCode(401)
            .body("authorized", is("not_authorized"));
    }

    @Test
    void testAuthorize_PartnerNotFound() {
        // Given: партнер не найден в списке
        var otherPartner = createPartner(999999, "2023-01-15");
        var response = new PartnerListResponse(null, List.of(otherPartner), null, null);

        when(greenwayService.getPartnerList(anyLong(), anyInt())).thenReturn(response);

        // When & Then: партнер не найден
        given()
            .auth().basic("admin", "admin")
            .contentType(ContentType.JSON)
            .body("""
                {
                    "greenwayId": 123456,
                    "regDate": "2023-01-15"
                }
                """)
            .when()
            .post("/greenway/authorize")
            .then()
            .statusCode(404)
            .body("authorized", is("not_authorized"));
    }

    @Test
    void testAuthorize_EmptyPartnerList() {
        // Given: список партнеров пустой
        var response = new PartnerListResponse(null, List.of(), null, null);

        when(greenwayService.getPartnerList(anyLong(), anyInt())).thenReturn(response);

        // When & Then: список пуст
        given()
            .auth().basic("admin", "admin")
            .contentType(ContentType.JSON)
            .body("""
                {
                    "greenwayId": 123456,
                    "regDate": "2023-01-15"
                }
                """)
            .when()
            .post("/greenway/authorize")
            .then()
            .statusCode(404)
            .body("authorized", is("not_authorized"));
    }

    @Test
    void testAuthorize_ApiException() {
        // Given: API выбрасывает исключение
        when(greenwayService.getPartnerList(anyLong(), anyInt()))
            .thenThrow(new GreenwayApiException("API Error", "ERROR_CODE", "Error details"));

        // When & Then: возвращается 500
        given()
            .auth().basic("admin", "admin")
            .contentType(ContentType.JSON)
            .body("""
                {
                    "greenwayId": 123456,
                    "regDate": "2023-01-15"
                }
                """)
            .when()
            .post("/greenway/authorize")
            .then()
            .statusCode(500)
            .body("authorized", is("not_authorized"));
    }

    // ==================== CheckUserId Tests ====================

    @Test
    void testCheckUserId_Success() {
        // Given: партнер существует
        var partner = createPartner(123456, "2023-01-15");
        var response = new PartnerListResponse(null, List.of(partner), null, null);

        when(greenwayService.getPartnerList(anyLong(), anyInt())).thenReturn(response);
        when(greenwayService.findPartnerById(any(), anyLong()))
                .thenReturn(java.util.Optional.of(partner));

        // When & Then: партнер найден
        given()
                .auth().basic("admin", "admin")
                .when()
                .get("/greenway/check-user/123456")
                .then()
                .statusCode(200)
                .body("userId", is(123456));
    }

    @Test
    void testCheckUserId_NotFound() {
        // Given: партнер не существует
        var response = new PartnerListResponse(null, List.of(), null, null);

        when(greenwayService.getPartnerList(anyLong(), anyInt())).thenReturn(response);
        when(greenwayService.findPartnerById(any(), anyLong()))
                .thenReturn(java.util.Optional.empty());

        // When & Then: партнер не найден
        given()
                .auth().basic("admin", "admin")
                .when()
                .get("/greenway/check-user/999999")
                .then()
                .statusCode(404);
    }

    // ==================== CompareLO Tests ====================

    @Test
    void testCompareLO_Greater() {
        // Given: у партнера LO больше чем переданное значение
        var partner = createPartnerWithLO(123456, 150.0);
        var response = new PartnerListResponse(null, List.of(partner), null, null);

        when(greenwayService.getPartnerList(anyLong(), anyInt())).thenReturn(response);
        when(greenwayService.findPartnerById(any(), anyLong()))
                .thenReturn(java.util.Optional.of(partner));

        // When & Then: результат "greater"
        given()
                .auth().basic("admin", "admin")
                .when()
                .get("/greenway/compare-lo/123456/100.0")
                .then()
                .statusCode(200)
                .body("userId", is(123456))
                .body("lo", is(150.0f))
                .body("loComparisonResult", is("greater"))
                .body("period", is(0));
    }

    @Test
    void testCompareLO_Less() {
        // Given: у партнера LO меньше чем переданное значение
        var partner = createPartnerWithLO(123456, 50.0);
        var response = new PartnerListResponse(null, List.of(partner), null, null);

        when(greenwayService.getPartnerList(anyLong(), anyInt())).thenReturn(response);
        when(greenwayService.findPartnerById(any(), anyLong()))
                .thenReturn(java.util.Optional.of(partner));

        // When & Then: результат "less"
        given()
                .auth().basic("admin", "admin")
                .when()
                .get("/greenway/compare-lo/123456/100.0")
                .then()
                .statusCode(200)
                .body("loComparisonResult", is("less"));
    }

    @Test
    void testCompareLO_Equal() {
        // Given: у партнера LO равно переданному значению
        var partner = createPartnerWithLO(123456, 100.0);
        var response = new PartnerListResponse(null, List.of(partner), null, null);

        when(greenwayService.getPartnerList(anyLong(), anyInt())).thenReturn(response);
        when(greenwayService.findPartnerById(any(), anyLong()))
                .thenReturn(java.util.Optional.of(partner));

        // When & Then: результат "equal"
        given()
                .auth().basic("admin", "admin")
                .when()
                .get("/greenway/compare-lo/123456/100.0")
                .then()
                .statusCode(200)
                .body("loComparisonResult", is("equal"));
    }

    @Test
    void testCompareLOPeriod_Success() {
        // Given: партнер существует в предыдущем периоде
        var partner = createPartnerWithLO(123456, 200.0);
        var response = new PartnerListResponse(null, List.of(partner), null, null);

        when(greenwayService.getPreviousPeriod()).thenReturn(75);
        when(greenwayService.getPartnerList(anyLong(), anyInt())).thenReturn(response);
        when(greenwayService.findPartnerById(any(), anyLong()))
                .thenReturn(java.util.Optional.of(partner));

        // When & Then: результат с указанием периода
        given()
                .auth().basic("admin", "admin")
                .when()
                .get("/greenway/compare-lo/period/123456/100.0")
                .then()
                .statusCode(200)
                .body("loComparisonResult", is("greater"))
                .body("period", is(75));
    }

    // ==================== CompareSGO Tests ====================

    @Test
    void testCompareSGO_Greater() {
        // Given: у партнера SGO больше чем переданное значение
        var partner = createPartnerWithSGO(123456, 500.0);
        var response = new PartnerListResponse(null, List.of(partner), null, null);

        when(greenwayService.getPartnerList(anyLong(), anyInt())).thenReturn(response);
        when(greenwayService.findPartnerById(any(), anyLong()))
                .thenReturn(java.util.Optional.of(partner));

        // When & Then: результат "greater"
        given()
                .auth().basic("admin", "admin")
                .when()
                .get("/greenway/compare-sgo/123456/300.0")
                .then()
                .statusCode(200)
                .body("userId", is(123456))
                .body("sgo", is(500.0f))
                .body("sgoComparisonResult", is("greater"));
    }

    @Test
    void testCompareSGO_NotFound() {
        // Given: партнер не найден
        var response = new PartnerListResponse(null, List.of(), null, null);

        when(greenwayService.getPartnerList(anyLong(), anyInt())).thenReturn(response);
        when(greenwayService.findPartnerById(any(), anyLong()))
                .thenReturn(java.util.Optional.empty());

        // When & Then: результат "not-found"
        given()
                .auth().basic("admin", "admin")
                .when()
                .get("/greenway/compare-sgo/999999/300.0")
                .then()
                .statusCode(200)
                .body("sgoComparisonResult", is("not-found"));
    }

    // ==================== Qualification Tests ====================

    @Test
    void testGetQualification_Success() {
        // Given: партнер с квалификацией L2
        var partner = createPartnerWithQualification(123456, "L2");
        var response = new PartnerListResponse(null, List.of(partner), null, null);

        when(greenwayService.getPartnerList(anyLong(), anyInt())).thenReturn(response);
        when(greenwayService.findPartnerById(any(), anyLong()))
                .thenReturn(java.util.Optional.of(partner));

        // When & Then: возвращается только буква "L"
        given()
                .auth().basic("admin", "admin")
                .when()
                .get("/greenway/qualification/123456")
                .then()
                .statusCode(200)
                .body("qualification", is("L"));
    }

    @Test
    void testGetQualificationExact_Success() {
        // Given: партнер с квалификацией L2
        var partner = createPartnerWithQualification(123456, "L2");
        var response = new PartnerListResponse(null, List.of(partner), null, null);

        when(greenwayService.getPartnerList(anyLong(), anyInt())).thenReturn(response);
        when(greenwayService.findPartnerById(any(), anyLong()))
                .thenReturn(java.util.Optional.of(partner));

        // When & Then: возвращается полная квалификация "L2"
        given()
                .auth().basic("admin", "admin")
                .when()
                .get("/greenway/qualification/exact/123456")
                .then()
                .statusCode(200)
                .body("qualification", is("L2"));
    }

    @Test
    void testGetQualificationPeriod_Success() {
        // Given: партнер с квалификацией M1 в прошлом периоде
        var partner = createPartnerWithQualification(123456, "M1");
        var response = new PartnerListResponse(null, List.of(partner), null, null);

        when(greenwayService.getPreviousPeriod()).thenReturn(75);
        when(greenwayService.getPartnerList(anyLong(), anyInt())).thenReturn(response);
        when(greenwayService.findPartnerById(any(), anyLong()))
                .thenReturn(java.util.Optional.of(partner));

        // When & Then: возвращается "M"
        given()
                .auth().basic("admin", "admin")
                .when()
                .get("/greenway/qualification/period/123456")
                .then()
                .statusCode(200)
                .body("qualification", is("M"));
    }

    @Test
    void testGetQualificationBest_CurrentBetter() {
        // Given: текущая квалификация лучше предыдущей (M > L)
        var currentPartner = createPartnerWithQualification(123456, "M1");
        var previousPartner = createPartnerWithQualification(123456, "L2");

        var currentResponse = new PartnerListResponse(null, List.of(currentPartner), null, null);
        var previousResponse = new PartnerListResponse(null, List.of(previousPartner), null, null);

        when(greenwayService.getPreviousPeriod()).thenReturn(75);
        when(greenwayService.getPartnerList(123456L, 0))
                .thenReturn(currentResponse);
        when(greenwayService.getPartnerList(123456L, 75))
                .thenReturn(previousResponse);
        when(greenwayService.findPartnerById(eq(currentResponse), anyLong()))
                .thenReturn(java.util.Optional.of(currentPartner));
        when(greenwayService.findPartnerById(eq(previousResponse), anyLong()))
                .thenReturn(java.util.Optional.of(previousPartner));

        // When & Then: возвращается "M"
        given()
                .auth().basic("admin", "admin")
                .when()
                .get("/greenway/qualification/best/123456")
                .then()
                .statusCode(200)
                .body("qualification", is("M"));
    }

    @Test
    void testGetQualificationBest_PreviousBetter() {
        // Given: предыдущая квалификация лучше текущей (GM > S)
        var currentPartner = createPartnerWithQualification(123456, "S1");
        var previousPartner = createPartnerWithQualification(123456, "GM4");

        var currentResponse = new PartnerListResponse(null, List.of(currentPartner), null, null);
        var previousResponse = new PartnerListResponse(null, List.of(previousPartner), null, null);

        when(greenwayService.getPreviousPeriod()).thenReturn(75);
        when(greenwayService.getPartnerList(123456L, 0))
                .thenReturn(currentResponse);
        when(greenwayService.getPartnerList(123456L, 75))
                .thenReturn(previousResponse);
        when(greenwayService.findPartnerById(eq(currentResponse), anyLong()))
                .thenReturn(java.util.Optional.of(currentPartner));
        when(greenwayService.findPartnerById(eq(previousResponse), anyLong()))
                .thenReturn(java.util.Optional.of(previousPartner));

        // When & Then: возвращается "GM"
        given()
                .auth().basic("admin", "admin")
                .when()
                .get("/greenway/qualification/best/123456")
                .then()
                .statusCode(200)
                .body("qualification", is("GM"));
    }

    @Test
    void testGetQualification_NotFound() {
        // Given: партнер не найден
        var response = new PartnerListResponse(null, List.of(), null, null);

        when(greenwayService.getPartnerList(anyLong(), anyInt())).thenReturn(response);
        when(greenwayService.findPartnerById(any(), anyLong()))
                .thenReturn(java.util.Optional.empty());

        // When & Then: возвращается 200 с NO (типизированный response)
        given()
                .auth().basic("admin", "admin")
                .when()
                .get("/greenway/qualification/999999")
                .then()
                .statusCode(200)
                .body("qualification", is("NO"));
    }

    // ==================== Helper Methods ====================

    /**
     * Вспомогательный метод для создания тестового партнера
     */
    private Partner createPartner(int number, String regDate) {
        return new Partner(
            number,           // id
            "Иванов",         // lastName
            "Иван",           // firstName
            "Иванович",       // patronymic
            "1990-01-01",     // birthday
            "test@example.com", // email
            "+79001234567",   // phone
            number,           // number
            "ACTIVE",         // agreementState
            regDate,          // regDate
            "Россия",         // countryName
            1,                // cityId
            "Москва",         // cityName
            null,             // vk
            null,             // telegram
            null,             // instagram
            null,             // whatsapp
            0.0,              // lo
            0.0,              // lgo
            0.0,              // sgo
            "NO",             // qualification
            1,                // level
            false,            // hasChildren
            "Петров Петр"     // mentorFullName
        );
    }

    private Partner createPartnerWithLO(int number, double lo) {
        return new Partner(
                number,
                "Иванов",
                "Иван",
                "Иванович",
                "1990-01-01",
                "test@example.com",
                "+79001234567",
                number,
                "ACTIVE",
                "2023-01-15",
                "Россия",
                1,
                "Москва",
                null,
                null,
                null,
                null,
                lo,              // lo
                0.0,
                0.0,
                "NO",
                1,
                false,
                "Петров Петр"
        );
    }

    private Partner createPartnerWithSGO(int number, double sgo) {
        return new Partner(
                number,
                "Иванов",
                "Иван",
                "Иванович",
                "1990-01-01",
                "test@example.com",
                "+79001234567",
                number,
                "ACTIVE",
                "2023-01-15",
                "Россия",
                1,
                "Москва",
                null,
                null,
                null,
                null,
                0.0,
                0.0,
                sgo,             // sgo
                "NO",
                1,
                false,
                "Петров Петр"
        );
    }

    private Partner createPartnerWithQualification(int number, String qualification) {
        return new Partner(
                number,
                "Иванов",
                "Иван",
                "Иванович",
                "1990-01-01",
                "test@example.com",
                "+79001234567",
                number,
                "ACTIVE",
                "2023-01-15",
                "Россия",
                1,
                "Москва",
                null,
                null,
                null,
                null,
                0.0,
                0.0,
                0.0,
                qualification,   // qualification
                1,
                false,
                "Петров Петр"
        );
    }
}
