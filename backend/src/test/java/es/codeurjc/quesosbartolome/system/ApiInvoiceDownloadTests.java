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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "spring.profiles.active=test")
public class ApiInvoiceDownloadTests {

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

        return login(name, password);
    }

    private io.restassured.http.Cookies login(String name, String password) throws JSONException {
        JSONObject loginBody = new JSONObject();
        loginBody.put("username", name);
        loginBody.put("password", password);

        return given()
                .contentType("application/json")
                .body(loginBody.toString())
                .post("/api/v1/auth/login")
                .then()
                .statusCode(anyOf(is(200), is(201)))
                .extract()
                .detailedCookies();
    }

    private io.restassured.http.Cookies loginAsAdmin() throws JSONException {
        return login("German", "password123");
    }

    @Test
    void testDownloadInvoicePdfNotFound() throws Exception {
        var adminCookies = loginAsAdmin();

        given()
                .cookies(adminCookies)
                .when()
                .get("/api/v1/invoices/999999/download-pdf")
                .then()
                .statusCode(404);
    }

    @Test
    void testDownloadInvoicePdfSuccess() throws Exception {
        // GIVEN: Create an invoice
        String uniqueUser = "PdfTestUser" + System.nanoTime();
        var userCookies = registerAndLoginTestUser(uniqueUser, "password123");
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

        var invoiceResponse = given()
                .cookies(adminCookies)
                .contentType("application/json")
                .body("{\"id\": " + orderId + "}")
                .when()
                .post("/api/v1/invoices")
                .then()
                .statusCode(anyOf(is(200), is(201)))
                .extract()
                .jsonPath();

        int invoiceId = invoiceResponse.getInt("id");

        byte[] pdfResponse = given()
            .cookies(adminCookies)
                .when()
                .get("/api/v1/invoices/" + invoiceId + "/download-pdf")
                .then()
                .statusCode(200)
                .contentType("application/pdf")
                .header("Content-Disposition", containsString("attachment"))
                .extract()
                .body()
                .asByteArray();

        assert pdfResponse.length > 0 : "PDF should not be empty";
        assert pdfResponse[0] == '%' : "PDF should start with %";
        assert pdfResponse[1] == 'P' : "PDF header should be valid";
    }
}
