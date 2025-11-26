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
public class ApiUserTests {

    @LocalServerPort
    int port;

    @BeforeEach
    void setup() {
        RestAssured.port = port;
        RestAssured.baseURI = "https://localhost";
        RestAssured.useRelaxedHTTPSValidation();
    }

    @Test
    void testGetCurrentUserUnauthorized() {
        // Without login, should return 401 Unauthorized
        given()
            .when()
            .get("/api/v1/users")
            .then()
            .statusCode(401)
            .body(is(emptyOrNullString()));
    }

    @Test
    void testGetCurrentUserAfterLogin() throws JSONException {
        // Register test user
        JSONObject registerBody = new JSONObject();
        registerBody.put("name", "Jorge");
        registerBody.put("password", "password123");
        registerBody.put("gmail", "jorge@example.com");
        registerBody.put("direction", "Calle Victoria 1");
        registerBody.put("nif", "87654321B");
        registerBody.put("image", JSONObject.NULL);

        given()
            .contentType("application/json")
            .body(registerBody.toString())
            .post("/api/v1/auth/register");

        // Login
        JSONObject loginBody = new JSONObject();
        loginBody.put("username", "Jorge");
        loginBody.put("password", "password123");

        // Store the cookies from the login response
        var responseCookies = given()
            .contentType("application/json")
            .body(loginBody.toString())
            .post("/api/v1/auth/login")
            .then()
            .statusCode(200)
            .extract()
            .detailedCookies();

        // Get current user with the stored cookies
        given()
            .cookies(responseCookies)
            .when()
            .get("/api/v1/users")
            .then()
            .statusCode(200)
            .body("name", equalTo("Jorge"))
            .body("gmail", equalTo("jorge@example.com"))
            .body("direction", equalTo("Calle Victoria 1"))
            .body("nif", equalTo("87654321B"))
            .body("id", notNullValue())
            .body("password", notNullValue());
    }

    @Test
    void testGetUserImageById() throws JSONException {
        JSONObject registerBody = new JSONObject();
        registerBody.put("name", "Ana");
        registerBody.put("password", "clave456");
        registerBody.put("gmail", "ana@example.com");
        registerBody.put("direction", "Calle Luna 2");
        registerBody.put("nif", "12345678C");
        registerBody.put("image", JSONObject.NULL);

        var registerResponse = given()
            .contentType("application/json")
            .body(registerBody.toString())
            .post("/api/v1/auth/register")
            .then()
            .statusCode(201)
            .extract()
            .response();

        Integer idInt = registerResponse.path("id");
        Long userId = idInt.longValue();

        given()
            .when()
            .get("/api/v1/users/" + userId + "/image")
            .then()
            .statusCode(204);
    }

    @Test
    void testGetUserImageByIdNotFound() throws JSONException {
        Long userId = 99999L; // Assuming this ID does not exist

        given()
            .when()
            .get("/api/v1/users/" + userId + "/image")
            .then()
            .statusCode(404);
    }

    @Test
    void testGetUserById() throws JSONException {
        // Registrar un usuario
        JSONObject registerBody = new JSONObject();
        registerBody.put("name", "Luis");
        registerBody.put("password", "password789");
        registerBody.put("gmail", "luis@example.com");
        registerBody.put("direction", "Calle Mayor 3");
        registerBody.put("nif", "11223344D");
        registerBody.put("image", JSONObject.NULL);

        var registerResponse = given()
            .contentType("application/json")
            .body(registerBody.toString())
            .post("/api/v1/auth/register")
            .then()
            .statusCode(201)
            .extract()
            .response();

        Integer idInt = registerResponse.path("id");
        Long userId = idInt.longValue();

        given()
            .when()
            .get("/api/v1/users/" + userId)
            .then()
            .statusCode(200)
            .body("name", equalTo("Luis"))
            .body("gmail", equalTo("luis@example.com"))
            .body("direction", equalTo("Calle Mayor 3"))
            .body("nif", equalTo("11223344D"))
            .body("id", equalTo(userId.intValue())); 
    }

    @Test
    void testGetUserByIdNotFound() {
        given()
            .when()
            .get("/api/v1/users/99999")
            .then()
            .statusCode(404);
    }


}
