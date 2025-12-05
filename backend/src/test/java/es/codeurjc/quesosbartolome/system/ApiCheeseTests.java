package es.codeurjc.quesosbartolome.system;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "spring.profiles.active=test"
)
public class ApiCheeseTests {
    @LocalServerPort
    int port;

    @BeforeEach
    void setup() {
        RestAssured.port = port;
        RestAssured.baseURI = "https://localhost";
        RestAssured.useRelaxedHTTPSValidation(); 
    }

    @Test
    void testGetAllCheeses() {
        when()
            .get("/api/v1/cheeses")
        .then()
            .statusCode(200)
            .body("$", not(empty()))
            .body("[0].name", equalTo("Semicurado"))
            .body("[1].name", equalTo("Azul"));
    }

    @Test
    void testGetCheeseById() {
        when()
            .get("/api/v1/cheeses/1")
        .then()
            .statusCode(200)
            .body("id", equalTo(1))
            .body("name", equalTo("Semicurado"));
    }

    @Test
    void testGetCheeseById_NotFound() {
        when()
            .get("/api/v1/cheeses/999")
        .then()
            .statusCode(404);
    }

    @Test
    void testGetCheeseImage_Ok() {
        when()
            .get("/api/v1/cheeses/1/image")
        .then()
            .statusCode(200)
            .header("Content-Type", equalTo("image/png"))
            .body(not(empty()));
    }

    // Added test for image 204 when i created the controller method POST

    @Test
    void testGetCheeseImage_NotFound() {
        when()
            .get("/api/v1/cheeses/999/image")
        .then()
            .statusCode(404);
    }
}
