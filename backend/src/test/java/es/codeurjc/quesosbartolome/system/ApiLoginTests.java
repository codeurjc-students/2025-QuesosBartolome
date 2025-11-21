package es.codeurjc.quesosbartolome.system;

import io.restassured.RestAssured;

import org.json.JSONException;
import org.json.JSONObject;
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
public class ApiLoginTests {

    @LocalServerPort
    int port;

    @BeforeEach
    void setup() {
        RestAssured.port = port;
        RestAssured.baseURI = "https://localhost";
        RestAssured.useRelaxedHTTPSValidation();
    }

    @Test
    void testRegisterUserSuccessfully() throws JSONException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("name", "Juan");
        requestBody.put("password", "password123");
        requestBody.put("gmail", "juan@example.com");
        requestBody.put("direction", "Calle Falsa 123");
        requestBody.put("nif", "12345678A");
        requestBody.put("image", JSONObject.NULL); 

        given()
            .contentType("application/json")
            .body(requestBody.toString())
        .when()
            .post("/api/v1/auth/register")
        .then()
            .statusCode(201)
            .body("message", equalTo("User registered successfully"));
    }

    @Test
    void testRegisterUserBadRequest() throws JSONException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("name", ""); // Blank name to trigger bad request
        requestBody.put("password", "password123");
        requestBody.put("gmail", "juan@example.com");
        requestBody.put("direction", "Calle Falsa 123");
        requestBody.put("nif", "12345678A");
        requestBody.put("image", JSONObject.NULL);
        given()
            .contentType("application/json")
            .body(requestBody.toString())
        .when()
            .post("/api/v1/auth/register")
        .then()
            .statusCode(400)
            .body("error", equalTo("Missing or blank fields"));
    }

    @Test
    void testLoginUserSuccessfully() throws JSONException {
        JSONObject registerBody = new JSONObject();
        registerBody.put("name", "Victor");
        registerBody.put("password", "password123");
        registerBody.put("gmail", "victor@example.com");
        registerBody.put("direction", "Calle Victoria 1");
        registerBody.put("nif", "87654321B");
        registerBody.put("image", JSONObject.NULL);
        given()
            .contentType("application/json")
            .body(registerBody.toString())
        .post("/api/v1/auth/register");

        JSONObject loginBody = new JSONObject();
        loginBody.put("username", "Victor");
        loginBody.put("password", "password123");
        given()
            .contentType("application/json")
            .body(loginBody.toString())
        .when()
            .post("/api/v1/auth/login")
        .then()
            .statusCode(200)
            .body("status", equalTo("SUCCESS"))
            .body("message", containsString("Tokens are created in cookie"));
    }

    @Test
    void testLogoutUser() {
        given()
        .when()
            .post("/api/v1/auth/logout")
        .then()
            .statusCode(200)
            .body("status", equalTo("SUCCESS"))
            .body("message", equalTo("logout successfully"));
    }
}
