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
public class ApiCartTests {

    @LocalServerPort
    int port;

    @BeforeEach
    void setup() {
        RestAssured.port = port;
        RestAssured.baseURI = "https://localhost";
        RestAssured.useRelaxedHTTPSValidation();
    }

    /**
     * Helper to register and log in a test user,
     * returning the session cookies.
     */
    private io.restassured.http.Cookies registerAndLoginTestUser(String name, String password) throws JSONException {
        // Register user
        JSONObject registerBody = new JSONObject();
        registerBody.put("name", name);
        registerBody.put("password", password);
        registerBody.put("gmail", name.toLowerCase() + "@example.com");
        registerBody.put("direction", "Street of " + name);
        registerBody.put("nif", "12345678Z");
        registerBody.put("image", JSONObject.NULL);

        given()
            .contentType("application/json")
            .body(registerBody.toString())
            .post("/api/v1/auth/register")
            .then()
            .statusCode(anyOf(is(200), is(201))); // depending on your API it may return 200 or 201

        // Login
        JSONObject loginBody = new JSONObject();
        loginBody.put("username", name);
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
    void testGetMyCart_Unauthorized() {
        when()
            .get("/api/v1/cart")
        .then()
            .statusCode(401);
    }

    @Test
    void testGetMyCart_Ok() throws JSONException {
        var cookies = registerAndLoginTestUser("CartUser", "password123");

        given()
            .cookies(cookies)
        .when()
            .get("/api/v1/cart")
        .then()
            .statusCode(200)
            .body("id", notNullValue())
            .body("items", notNullValue());
    }

    @Test
    void testAddItemToCart_Ok() throws JSONException {
        var cookies = registerAndLoginTestUser("CartUser2", "password123");

        given()
            .cookies(cookies)
            .queryParam("cheeseId", 1)
            .queryParam("boxes", 1)
        .when()
            .put("/api/v1/cart/addItem")
        .then()
            .statusCode(200)
            .body("items", not(empty()));
    }

    @Test
    void testAddItemToCart_BadRequest() throws JSONException {
        var cookies = registerAndLoginTestUser("CartUser3", "password123");

        given()
            .cookies(cookies)
            .queryParam("cheeseId", 1)
            .queryParam("boxes", 0)
        .when()
            .put("/api/v1/cart/addItem")
        .then()
            .statusCode(400);
    }

    @Test
    void testRemoveItemFromCart_Ok() throws JSONException {
        var cookies = registerAndLoginTestUser("CartUser4", "password123");

        // First add an item
        given()
            .cookies(cookies)
            .queryParam("cheeseId", 1)
            .queryParam("boxes", 1)
        .when()
            .put("/api/v1/cart/addItem")
        .then()
            .statusCode(200);

        // Now remove the item with id 1 (example)
        given()
            .cookies(cookies)
            .queryParam("itemId", 1)
        .when()
            .put("/api/v1/cart/removeItem")
        .then()
            .statusCode(200)
            .body("items", anyOf(empty(), notNullValue()));
    }

    @Test
    void testRemoveItemFromCart_Unauthorized() {
        given()
            .queryParam("itemId", 1)
        .when()
            .put("/api/v1/cart/removeItem")
        .then()
            .statusCode(401);
    }
}
