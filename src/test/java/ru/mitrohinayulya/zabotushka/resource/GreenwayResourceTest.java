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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
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
    void testAuthorize_DateMismatch() {
        // Given: партнер существует, но дата регистрации не совпадает
        var partner = createPartner(123456, "2023-01-15");
        var response = new PartnerListResponse(null, List.of(partner), null, null);

        when(greenwayService.getPartnerList(anyLong(), anyInt())).thenReturn(response);

        // When & Then: авторизация не удалась (неправильная дата)
        given()
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
}
