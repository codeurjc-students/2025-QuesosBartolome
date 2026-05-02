package es.codeurjc.quesosbartolome.system;

import io.restassured.RestAssured;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "spring.profiles.active=test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class ApiInvoiceTests {

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

        private io.restassured.http.Cookies loginAsAdmin() throws JSONException {
                return login("Admin", "password123");
        }

        @Test
        void testGetAllInvoicesList_Unauthorized() {
                given()
                                .when()
                                .get("/api/v1/invoices/all")
                                .then()
                                .statusCode(401);
        }

        @Test
        void testGetAllInvoicesList_ForbiddenForNonAdmin() throws JSONException {
                var userCookies = login("Tienda Artesanal de Riaza", "password123");

                given()
                                .cookies(userCookies)
                                .when()
                                .get("/api/v1/invoices/all")
                                .then()
                                .statusCode(403);
        }

        @Test
        void testGetAllInvoicesList_OkAsAdmin() throws JSONException {
                var adminCookies = loginAsAdmin();

                given()
                                .cookies(adminCookies)
                                .when()
                                .get("/api/v1/invoices/all")
                                .then()
                                .statusCode(200)
                                .body("size()", greaterThanOrEqualTo(0));
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
                var userCookies = login("Tienda Artesanal de Riaza", "password123");
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
                var userCookies = login("Tienda Artesanal de Riaza", "password123");
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

        @Test
        void testCreateInvoice_Ok() throws Exception {
                var userCookies = login("Tienda Artesanal de Riaza", "password123");
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
