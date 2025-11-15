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
public class apiTests {
    @LocalServerPort
    int port;

    @BeforeEach
    void setup() {
        RestAssured.port = port;
        RestAssured.baseURI = "https://localhost";
        RestAssured.useRelaxedHTTPSValidation(); // ignorar certificados auto-firmados
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
}
