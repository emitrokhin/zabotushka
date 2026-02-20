package ru.mitrohinayulya.zabotushka.resource;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.mitrohinayulya.zabotushka.GreenwayServiceTestProfile;
import ru.mitrohinayulya.zabotushka.dto.greenway.Partner;
import ru.mitrohinayulya.zabotushka.dto.greenway.PartnerListResponse;
import ru.mitrohinayulya.zabotushka.service.greenway.GreenwayService;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@QuarkusTest
@TestProfile(GreenwayServiceTestProfile.class)
class GreenwayResourceTest {

    @InjectMock
    GreenwayService greenwayService;

    @Test
    @DisplayName("checkUserId returns 200 with user data when partner exists")
    void checkUserId_ShouldReturn200WithUser_WhenPartnerExists() {
        var partner = createPartner(123456);
        var response = new PartnerListResponse(null, List.of(partner), null, null);

        when(greenwayService.getPartnerList(anyLong(), anyInt())).thenReturn(response);
        when(greenwayService.findPartnerById(any(), anyLong()))
                .thenReturn(java.util.Optional.of(partner));

        given()
                .auth().basic("admin", "admin")
                .when()
                .get("/greenway/check-user/123456")
                .then()
                .statusCode(200)
                .body("userId", is(123456));
    }

    @Test
    @DisplayName("checkUserId returns 404 when partner not found")
    void checkUserId_ShouldReturn404_WhenPartnerNotFound() {
        var response = new PartnerListResponse(null, List.of(), null, null);

        when(greenwayService.getPartnerList(anyLong(), anyInt())).thenReturn(response);
        when(greenwayService.findPartnerById(any(), anyLong()))
                .thenReturn(java.util.Optional.empty());

        given()
                .auth().basic("admin", "admin")
                .when()
                .get("/greenway/check-user/999999")
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("compareLO returns 'greater' when partner LO exceeds the threshold")
    void compareLO_ShouldReturnGreater_WhenPartnerLOExceedsThreshold() {
        var partner = createPartnerWithLO(150.0);
        var response = new PartnerListResponse(null, List.of(partner), null, null);

        when(greenwayService.getPartnerList(anyLong(), anyInt())).thenReturn(response);
        when(greenwayService.findPartnerById(any(), anyLong()))
                .thenReturn(java.util.Optional.of(partner));

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
    @DisplayName("compareLO returns 'less' when partner LO is below the threshold")
    void compareLO_ShouldReturnLess_WhenPartnerLOIsBelowThreshold() {
        var partner = createPartnerWithLO(50.0);
        var response = new PartnerListResponse(null, List.of(partner), null, null);

        when(greenwayService.getPartnerList(anyLong(), anyInt())).thenReturn(response);
        when(greenwayService.findPartnerById(any(), anyLong()))
                .thenReturn(java.util.Optional.of(partner));

        given()
                .auth().basic("admin", "admin")
                .when()
                .get("/greenway/compare-lo/123456/100.0")
                .then()
                .statusCode(200)
                .body("loComparisonResult", is("less"));
    }

    @Test
    @DisplayName("compareLO returns 'equal' when partner LO equals the threshold")
    void compareLO_ShouldReturnEqual_WhenPartnerLOEqualsThreshold() {
        var partner = createPartnerWithLO(100.0);
        var response = new PartnerListResponse(null, List.of(partner), null, null);

        when(greenwayService.getPartnerList(anyLong(), anyInt())).thenReturn(response);
        when(greenwayService.findPartnerById(any(), anyLong()))
                .thenReturn(java.util.Optional.of(partner));

        given()
                .auth().basic("admin", "admin")
                .when()
                .get("/greenway/compare-lo/123456/100.0")
                .then()
                .statusCode(200)
                .body("loComparisonResult", is("equal"));
    }

    @Test
    @DisplayName("compareLOPeriod returns comparison result with period when partner exists in previous period")
    void compareLOPeriod_ShouldReturnResultWithPeriod_WhenPartnerExistsInPreviousPeriod() {
        var partner = createPartnerWithLO(200.0);
        var response = new PartnerListResponse(null, List.of(partner), null, null);

        when(greenwayService.getPreviousPeriod()).thenReturn(75);
        when(greenwayService.getPartnerList(anyLong(), anyInt())).thenReturn(response);
        when(greenwayService.findPartnerById(any(), anyLong()))
                .thenReturn(java.util.Optional.of(partner));

        given()
                .auth().basic("admin", "admin")
                .when()
                .get("/greenway/compare-lo/period/123456/100.0")
                .then()
                .statusCode(200)
                .body("loComparisonResult", is("greater"))
                .body("period", is(75));
    }

    @Test
    @DisplayName("compareSGO returns 'greater' when partner SGO exceeds the threshold")
    void compareSGO_ShouldReturnGreater_WhenPartnerSGOExceedsThreshold() {
        var partner = createPartnerWithSGO();
        var response = new PartnerListResponse(null, List.of(partner), null, null);

        when(greenwayService.getPartnerList(anyLong(), anyInt())).thenReturn(response);
        when(greenwayService.findPartnerById(any(), anyLong()))
                .thenReturn(java.util.Optional.of(partner));

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
    @DisplayName("compareSGO returns 'not-found' when partner does not exist")
    void compareSGO_ShouldReturnNotFound_WhenPartnerNotFound() {
        var response = new PartnerListResponse(null, List.of(), null, null);

        when(greenwayService.getPartnerList(anyLong(), anyInt())).thenReturn(response);
        when(greenwayService.findPartnerById(any(), anyLong()))
                .thenReturn(java.util.Optional.empty());

        given()
                .auth().basic("admin", "admin")
                .when()
                .get("/greenway/compare-sgo/999999/300.0")
                .then()
                .statusCode(200)
                .body("sgoComparisonResult", is("not-found"));
    }

    @Test
    @DisplayName("getQualification returns base qualification letter when partner exists")
    void getQualification_ShouldReturnBaseQualification_WhenPartnerExists() {
        var partner = createPartnerWithQualification("L2");
        var response = new PartnerListResponse(null, List.of(partner), null, null);

        when(greenwayService.getPartnerList(anyLong(), anyInt())).thenReturn(response);
        when(greenwayService.findPartnerById(any(), anyLong()))
                .thenReturn(java.util.Optional.of(partner));

        given()
                .auth().basic("admin", "admin")
                .when()
                .get("/greenway/qualification/123456")
                .then()
                .statusCode(200)
                .body("qualification", is("L"));
    }

    @Test
    @DisplayName("getQualificationExact returns full qualification string when partner exists")
    void getQualificationExact_ShouldReturnFullQualification_WhenPartnerExists() {
        var partner = createPartnerWithQualification("L2");
        var response = new PartnerListResponse(null, List.of(partner), null, null);

        when(greenwayService.getPartnerList(anyLong(), anyInt())).thenReturn(response);
        when(greenwayService.findPartnerById(any(), anyLong()))
                .thenReturn(java.util.Optional.of(partner));

        given()
                .auth().basic("admin", "admin")
                .when()
                .get("/greenway/qualification/exact/123456")
                .then()
                .statusCode(200)
                .body("qualification", is("L2"));
    }

    @Test
    @DisplayName("getQualificationPeriod returns qualification from previous period")
    void getQualificationPeriod_ShouldReturnPreviousPeriodQualification_WhenPartnerExists() {
        var partner = createPartnerWithQualification("M1");
        var response = new PartnerListResponse(null, List.of(partner), null, null);

        when(greenwayService.getPreviousPeriod()).thenReturn(75);
        when(greenwayService.getPartnerList(anyLong(), anyInt())).thenReturn(response);
        when(greenwayService.findPartnerById(any(), anyLong()))
                .thenReturn(java.util.Optional.of(partner));

        given()
                .auth().basic("admin", "admin")
                .when()
                .get("/greenway/qualification/period/123456")
                .then()
                .statusCode(200)
                .body("qualification", is("M"));
    }

    @Test
    @DisplayName("getQualificationBest returns current period qualification when it is higher")
    void getQualificationBest_ShouldReturnCurrentQualification_WhenCurrentIsBetter() {
        var currentPartner = createPartnerWithQualification("M1");
        var previousPartner = createPartnerWithQualification("L2");

        var currentResponse = new PartnerListResponse(null, List.of(currentPartner), null, null);
        var previousResponse = new PartnerListResponse(null, List.of(previousPartner), null, null);

        when(greenwayService.getPreviousPeriod()).thenReturn(75);
        when(greenwayService.getPartnerList(123456L, 0)).thenReturn(currentResponse);
        when(greenwayService.getPartnerList(123456L, 75)).thenReturn(previousResponse);
        when(greenwayService.findPartnerById(eq(currentResponse), anyLong()))
                .thenReturn(java.util.Optional.of(currentPartner));
        when(greenwayService.findPartnerById(eq(previousResponse), anyLong()))
                .thenReturn(java.util.Optional.of(previousPartner));

        given()
                .auth().basic("admin", "admin")
                .when()
                .get("/greenway/qualification/best/123456")
                .then()
                .statusCode(200)
                .body("qualification", is("M"));
    }

    @Test
    @DisplayName("getQualificationBest returns previous period qualification when it is higher")
    void getQualificationBest_ShouldReturnPreviousQualification_WhenPreviousIsBetter() {
        var currentPartner = createPartnerWithQualification("S1");
        var previousPartner = createPartnerWithQualification("GM4");

        var currentResponse = new PartnerListResponse(null, List.of(currentPartner), null, null);
        var previousResponse = new PartnerListResponse(null, List.of(previousPartner), null, null);

        when(greenwayService.getPreviousPeriod()).thenReturn(75);
        when(greenwayService.getPartnerList(123456L, 0)).thenReturn(currentResponse);
        when(greenwayService.getPartnerList(123456L, 75)).thenReturn(previousResponse);
        when(greenwayService.findPartnerById(eq(currentResponse), anyLong()))
                .thenReturn(java.util.Optional.of(currentPartner));
        when(greenwayService.findPartnerById(eq(previousResponse), anyLong()))
                .thenReturn(java.util.Optional.of(previousPartner));

        given()
                .auth().basic("admin", "admin")
                .when()
                .get("/greenway/qualification/best/123456")
                .then()
                .statusCode(200)
                .body("qualification", is("GM"));
    }

    @Test
    @DisplayName("getQualification returns NO when partner not found")
    void getQualification_ShouldReturnNO_WhenPartnerNotFound() {
        var response = new PartnerListResponse(null, List.of(), null, null);

        when(greenwayService.getPartnerList(anyLong(), anyInt())).thenReturn(response);
        when(greenwayService.findPartnerById(any(), anyLong()))
                .thenReturn(java.util.Optional.empty());

        given()
                .auth().basic("admin", "admin")
                .when()
                .get("/greenway/qualification/999999")
                .then()
                .statusCode(200)
                .body("qualification", is("NO"));
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

    private Partner createPartnerWithLO(double lo) {
        return new Partner(
                123456, "Иванов", "Иван", "Иванович",
                "01.01.1990", "test@example.com", "+79001234567",
                123456, "ACTIVE", "15.01.2023",
                "Россия", 1, "Москва",
                null, null, null, null,
                lo, 0.0, 0.0, "NO", 1, false, "Петров Петр"
        );
    }

    private Partner createPartnerWithSGO() {
        return new Partner(
                123456, "Иванов", "Иван", "Иванович",
                "01.01.1990", "test@example.com", "+79001234567",
                123456, "ACTIVE", "15.01.2023",
                "Россия", 1, "Москва",
                null, null, null, null,
                0.0, 0.0, 500.0, "NO", 1, false, "Петров Петр"
        );
    }

    private Partner createPartnerWithQualification(String qualification) {
        return new Partner(
                123456, "Иванов", "Иван", "Иванович",
                "01.01.1990", "test@example.com", "+79001234567",
                123456, "ACTIVE", "15.01.2023",
                "Россия", 1, "Москва",
                null, null, null, null,
                0.0, 0.0, 0.0, qualification, 1, false, "Петров Петр"
        );
    }
}
