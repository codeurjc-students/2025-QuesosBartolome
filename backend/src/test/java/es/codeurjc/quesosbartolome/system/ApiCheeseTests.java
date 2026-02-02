package es.codeurjc.quesosbartolome.system;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "spring.profiles.active=test")
public class ApiCheeseTests {
    @LocalServerPort
    int port;

    @BeforeEach
    void setup() {
        RestAssured.port = port;
        RestAssured.baseURI = "https://localhost";
        RestAssured.useRelaxedHTTPSValidation();
    }

    private io.restassured.http.Cookies login(String username, String password) throws JSONException {
        JSONObject loginBody = new JSONObject();
        loginBody.put("username", username);
        loginBody.put("password", password);

        return given()
                .contentType("application/json")
                .body(loginBody.toString())
                .post("/api/v1/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .detailedCookies();
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

    @Test
    void testGetCheeseImage_NotFound() {
        when()
                .get("/api/v1/cheeses/999/image")
                .then()
                .statusCode(404);
    }

    @Test
    void testCreateCheese_Unauthorized() {
        given()
                .contentType("application/json")
                .body("{\"name\":\"NuevoQueso\"}")
                .when()
                .post("/api/v1/cheeses/new")
                .then()
                .statusCode(401);
    }

    @Test
    void testCreateCheese_Forbidden() throws JSONException {
        var cookies = login("Victor", "password123"); // user

        given()
                .cookies(cookies)
                .contentType("application/json")
                .body("{\"name\":\"NuevoQueso\"}")
                .when()
                .post("/api/v1/cheeses/new")
                .then()
                .statusCode(403);
    }

    @Test
    void testCreateCheese_BadRequest() throws JSONException {
        var cookies = login("German", "password123"); // admin

        given()
                .cookies(cookies)
                .contentType("application/json")
                .body("{}") // DTO missing required fields
                .when()
                .post("/api/v1/cheeses/new")
                .then()
                .statusCode(400);
    }

    @Test
    void testCreateCheese_Created() throws JSONException {
        var cookies = login("German", "password123"); // admin

        JSONObject requestBody = new JSONObject();
        requestBody.put("id", JSONObject.NULL);
        requestBody.put("name", "NuevoQueso");
        requestBody.put("price", 12.5);
        requestBody.put("description", "desc");
        requestBody.put("manufactureDate", "2024-01-01");
        requestBody.put("expirationDate", "2025-01-01");
        requestBody.put("Type", "Curado");
        requestBody.put("boxes", new JSONArray(List.of(1.0, 2.0)));

        given()
                .cookies(cookies)
                .contentType("application/json")
                .body(requestBody.toString())
                .when()
                .post("/api/v1/cheeses/new")
                .then()
                .statusCode(201)
                .header("Location", containsString("/api/v1/cheeses/"))
                .body("name", equalTo("NuevoQueso"));
    }

    @Test
    void testUploadCheeseImage_Unauthorized() {
        given()
                .multiPart("file", "imagen.png", "fakecontent".getBytes())
                .when()
                .post("/api/v1/cheeses/1/image")
                .then()
                .statusCode(401);
    }

    @Test
    void testUploadCheeseImage_Forbidden() throws JSONException {
        var cookies = login("Victor", "password123"); // user

        given()
                .cookies(cookies)
                .multiPart("file", "imagen.png", "fakecontent".getBytes())
                .when()
                .post("/api/v1/cheeses/1/image")
                .then()
                .statusCode(403);
    }

    @Test
    void testUploadCheeseImage_NotFound() throws JSONException {
        var cookies = login("German", "password123"); // admin

        given()
                .cookies(cookies)
                .multiPart("file", "imagen.png", "fakecontent".getBytes())
                .when()
                .post("/api/v1/cheeses/999/image")
                .then()
                .statusCode(404);
    }

    @Test
    void testUploadCheeseImage_Ok() throws JSONException {
        var cookies = login("German", "password123"); // admin

        given()
                .cookies(cookies)
                .multiPart("file", "imagen.png", "fakecontent".getBytes())
                .when()
                .post("/api/v1/cheeses/1/image")
                .then()
                .statusCode(200);
    }

}
