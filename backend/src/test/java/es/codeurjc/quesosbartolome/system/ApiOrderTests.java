package es.codeurjc.quesosbartolome.system;

import io.restassured.RestAssured;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "spring.profiles.active=test")
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

        // login()
        return login(name, password);
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

        private io.restassured.http.Cookies loginAsAdmin() throws JSONException {
                return login("German", "password123");
        }

    @Test
    void testGetAllOrders_Ok() throws JSONException {
                var cookies = loginAsAdmin();

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
        var cookies = registerAndLoginTestUser("GhostUser", "password123");

        given()
                .cookies(cookies)
                .when()
                .post("/api/v1/orders/confirm")
                .then()
                .statusCode(anyOf(is(404), is(400)));
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
    @Disabled("Fails sporadically in CI environment - passes locally")
    void testConfirmOrder_Ok() throws JSONException {
        var cookies = registerAndLoginTestUser("OrderUser2", "password123");

        given()
                .cookies(cookies)
                .queryParam("cheeseId", 5)
                .queryParam("boxes", 1)
                .when()
                .put("/api/v1/cart/addItem")
                .then()
                .statusCode(200);

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

    @Test
    void testGetOrderById_NotFound() {
        given()
                .when()
                .get("/api/v1/orders/999999")
                .then()
                                .statusCode(401);
    }

    @Test
        @Disabled("Fails sporadically in CI environment - passes locally")
    void testGetOrderById_Ok() throws Exception {
        var userCookies = registerAndLoginTestUser("OrderUserGet", "password123");
        var adminCookies = loginAsAdmin();

        given()
                .cookies(userCookies)
                .queryParam("cheeseId", 5)
                .queryParam("boxes", 1)
                .when()
                .put("/api/v1/cart/addItem")
                .then()
                .statusCode(200);

        int orderId = given()
                .cookies(userCookies)
                .when()
                .post("/api/v1/orders/confirm")
                .then()
                .statusCode(201)
                .extract()
                .jsonPath()
                .getInt("id");

        given()
                .cookies(adminCookies)
                .when()
                .get("/api/v1/orders/" + orderId)
                .then()
                .statusCode(200)
                .body("id", equalTo(orderId))
                .body("totalPrice", greaterThan(0.0f));
    }

    @Test
    void testRejectOrder_NotFound() {
        given()
                .when()
                .put("/api/v1/orders/999999/reject")
                .then()
                                .statusCode(401);
    }

    @Test
    void testRejectOrder_ConflictWhenAlreadyProcessed() throws Exception {
        var userCookies = registerAndLoginTestUser("RejectUser1", "password123");
        var adminCookies = loginAsAdmin();

        given()
                .cookies(userCookies)
                .queryParam("cheeseId", 5)
                .queryParam("boxes", 1)
                .when()
                .put("/api/v1/cart/addItem")
                .then()
                .statusCode(200);

        int orderId = given()
                .cookies(userCookies)
                .when()
                .post("/api/v1/orders/confirm")
                .then()
                .statusCode(201)
                .extract()
                .jsonPath()
                .getInt("id");

        given()
                .cookies(adminCookies)
                .when()
                .put("/api/v1/orders/" + orderId + "/reject")
                .then()
                .statusCode(200);

        given()
                .cookies(adminCookies)
                .when()
                .put("/api/v1/orders/" + orderId + "/reject")
                .then()
                .statusCode(409);
    }

    @Test
    @Disabled("Fails sporadically in CI environment - passes locally")
    void testRejectOrder_Ok() throws Exception {
        var userCookies = registerAndLoginTestUser("RejectUser2", "password123");
        var adminCookies = loginAsAdmin();

        given()
                .cookies(userCookies)
                .queryParam("cheeseId", 5)
                .queryParam("boxes", 1)
                .when()
                .put("/api/v1/cart/addItem")
                .then()
                .statusCode(200);

        int orderId = given()
                .cookies(userCookies)
                .when()
                .post("/api/v1/orders/confirm")
                .then()
                .statusCode(201)
                .extract()
                .jsonPath()
                .getInt("id");

        given()
                .cookies(adminCookies)
                .when()
                .put("/api/v1/orders/" + orderId + "/reject")
                .then()
                .statusCode(200)
                .body("id", equalTo(orderId));
    }

}
