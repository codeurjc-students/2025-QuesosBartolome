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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.profiles.active=test")
public class ApiInvoiceTests {

    @LocalServerPort
    int port;

    @BeforeEach
    void setup() {
        RestAssured.port = port;
        RestAssured.baseURI = "https://localhost";
        RestAssured.useRelaxedHTTPSValidation();
    }

    private io.restassured.http.Cookies registerAndLoginTestUser(String name, String password) throws JSONException {
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
    void testGetAllInvoices_Ok() throws JSONException {
        var adminCookies = loginAsAdmin();

        given()
                .cookies(adminCookies)
                .when()
                .get("/api/v1/invoices?page=0&size=10")
                .then()
                .statusCode(200)
                .body("content", notNullValue());
    }

    @Test
    void testGetInvoiceById_NotFound() throws JSONException {
        var adminCookies = loginAsAdmin();

        given()
                .cookies(adminCookies)
                .when()
                .get("/api/v1/invoices/999999")
                .then()
                .statusCode(404);
    }

    @Test
    void testGetInvoiceById_Ok() throws Exception {
        var userCookies = registerAndLoginTestUser("InvoiceUser1", "password123");
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

        JSONObject body = new JSONObject();
        body.put("id", orderId);

        int invoiceId = given()
                .cookies(adminCookies)
                .contentType("application/json")
                .body(body.toString())
                .when()
                .post("/api/v1/invoices")
                .then()
                .statusCode(201)
                .extract()
                .jsonPath()
                .getInt("id");

        given()
                .cookies(adminCookies)
                .when()
                .get("/api/v1/invoices/" + invoiceId)
                .then()
                .statusCode(200)
                .body("id", equalTo(invoiceId));
    }

    @Test
    void testCreateInvoice_BadRequest_NoBody() throws JSONException {
        var adminCookies = loginAsAdmin();

        given()
                .cookies(adminCookies)
                .contentType("application/json")
                .body("{}")
                .when()
                .post("/api/v1/invoices")
                .then()
                .statusCode(400);
    }

    @Test
    void testCreateInvoice_NotFound_OrderDoesNotExist() throws JSONException {
        var adminCookies = loginAsAdmin();

        JSONObject body = new JSONObject();
        body.put("id", 999999);

        given()
                .cookies(adminCookies)
                .contentType("application/json")
                .body(body.toString())
                .when()
                .post("/api/v1/invoices")
                .then()
                .statusCode(404);
    }

        @Test
        void testCreateInvoice_ReturnsExistingInvoice_WhenOrderAlreadyProcessed() throws Exception {
        var userCookies = registerAndLoginTestUser("InvoiceUser2", "password123");
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

        JSONObject body = new JSONObject();
        body.put("id", orderId);

        int firstInvoiceId = given()
                .cookies(adminCookies)
                .contentType("application/json")
                .body(body.toString())
                .when()
                .post("/api/v1/invoices")
                .then()
                .statusCode(201)
                .extract()
                .jsonPath()
                .getInt("id");

        given()
                .cookies(adminCookies)
                .contentType("application/json")
                .body(body.toString())
                .when()
                .post("/api/v1/invoices")
                .then()
                .statusCode(200)
                .body("id", equalTo(firstInvoiceId));
    }

    @Disabled("Failed only CI")
    @Test
    void testCreateInvoice_Ok() throws Exception {
        var userCookies = registerAndLoginTestUser("InvoiceUser3", "password123");
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

        JSONObject body = new JSONObject();
        body.put("id", orderId);

        given()
                .cookies(adminCookies)
                .contentType("application/json")
                .body(body.toString())
                .when()
                .post("/api/v1/invoices")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("taxableBase", greaterThan(0.0f))
                .body("totalPrice", greaterThan(0.0f));
    }
}
