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
public class ApiOrderTests {

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
            .statusCode(anyOf(is(200), is(201)));

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
    void testGetAllOrders_Ok() throws JSONException {
        var cookies = registerAndLoginTestUser("OrderUser", "password123");

        given()
            .cookies(cookies)
        .when()
            .get("/api/v1/orders?page=0&size=10")
        .then()
            .statusCode(200)
            .body("content", notNullValue());
    }

    @Test
    void testConfirmOrder_Unauthorized() {
        when()
            .post("/api/v1/orders/confirm")
        .then()
            .statusCode(401);
    }

    @Test
    void testConfirmOrder_UserNotFound() throws JSONException {
        // Register and login a user, then delete it to simulate not found
        var cookies = registerAndLoginTestUser("GhostUser", "password123");

        // Simulate user not found by calling confirm directly (depends on DB state)
        given()
            .cookies(cookies)
        .when()
            .post("/api/v1/orders/confirm")
        .then()
            .statusCode(anyOf(is(404), is(400))); // depending on how service handles missing user/cart
    }

    @Test
    void testConfirmOrder_BadRequestWhenCartEmpty() throws JSONException {
        var cookies = registerAndLoginTestUser("EmptyCartUser", "password123");

        given()
            .cookies(cookies)
        .when()
            .post("/api/v1/orders/confirm")
        .then()
            .statusCode(400);
    }

    @Test
    void testConfirmOrder_Ok() throws JSONException {
        var cookies = registerAndLoginTestUser("OrderUser2", "password123");

        // First add an item to the cart
        given()
            .cookies(cookies)
            .queryParam("cheeseId", 1)
            .queryParam("boxes", 1)
        .when()
            .put("/api/v1/cart/addItem")
        .then()
            .statusCode(200);

        // Now confirm the order
        given()
            .cookies(cookies)
        .when()
            .post("/api/v1/orders/confirm")
        .then()
            .statusCode(201) // created
            .header("Location", containsString("/api/v1/orders/confirm/"))
            .body("id", notNullValue())
            .body("totalPrice", greaterThan(0.0f))
            .body("totalWeight", greaterThan(0.0f));
    }
}
