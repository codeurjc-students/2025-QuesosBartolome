package es.codeurjc.quesosbartolome.system;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class apiTests {

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 8080; 
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