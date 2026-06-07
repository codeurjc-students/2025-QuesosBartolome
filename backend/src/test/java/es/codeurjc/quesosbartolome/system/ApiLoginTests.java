package es.codeurjc.quesosbartolome.system;

import io.restassured.RestAssured;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.UUID;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "spring.profiles.active=test")
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
                String uniqueSuffix = UUID.randomUUID().toString().replace("-", "");
                String userName = "UserTest1_" + uniqueSuffix;
                String email = userName + "@example.com";
                String nif = String.format("%08d", Math.abs(uniqueSuffix.hashCode()) % 100000000) + "A";

                JSONObject requestBody = new JSONObject();
                requestBody.put("name", userName);
                requestBody.put("password", "password123");
                requestBody.put("gmail", email);
                requestBody.put("direction", "Calle Falsa 123");
                requestBody.put("nif", nif);
                requestBody.put("image", JSONObject.NULL);

                given()
                                .contentType("application/json")
                                .body(requestBody.toString())
                                .when()
                                .post("/api/v1/auth/register")
                                .then()
                                .statusCode(201)
                                .body("id", notNullValue())
                                .body("name", equalTo(userName))
                                .body("gmail", equalTo(email))
                                .body("direction", equalTo("Calle Falsa 123"))
                                .body("nif", equalTo(nif));
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
                                .body(is(emptyOrNullString()));
        }

        @Test
        void testLoginUserSuccessfully() throws JSONException {
                JSONObject registerBody = new JSONObject();
                registerBody.put("name", "UserTest2");
                registerBody.put("password", "password123");
                registerBody.put("gmail", "User@example.com");
                registerBody.put("direction", "Calle Victoria 1");
                registerBody.put("nif", "87654321B");
                registerBody.put("image", JSONObject.NULL);
                given()
                                .contentType("application/json")
                                .body(registerBody.toString())
                                .post("/api/v1/auth/register");

                JSONObject loginBody = new JSONObject();
                loginBody.put("username", "UserTest2");
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

        @Test
        void testLoginFailsWhenUserIsBanned() throws JSONException {

                JSONObject adminLogin = new JSONObject();
                adminLogin.put("username", "Admin");
                adminLogin.put("password", "password123");

                var adminResponse = given()
                                .contentType("application/json")
                                .body(adminLogin.toString())
                                .when()
                                .post("/api/v1/auth/login")
                                .then()
                                .statusCode(200)
                                .extract()
                                .response();

                var cookieHeaderValues = adminResponse.getHeaders().getValues("Set-Cookie");
                assertFalse(cookieHeaderValues == null || cookieHeaderValues.isEmpty(),
                                "Set-Cookie headers should be present after admin login");

                String cookieHeader = cookieHeaderValues.stream()
                                .map(setCookie -> setCookie.split(";", 2)[0])
                                .reduce((c1, c2) -> c1 + "; " + c2)
                                .orElse("");

                assertFalse(cookieHeader.isBlank(), "Cookie header should contain AccessToken and RefreshToken");

                JSONObject registerBody = new JSONObject();
                registerBody.put("name", "BannedUser");
                registerBody.put("password", "password123");
                registerBody.put("gmail", "banned@example.com");
                registerBody.put("direction", "Calle Baneada 1");
                registerBody.put("nif", "12345678Z");
                registerBody.put("image", JSONObject.NULL);

                var createdUser = given()
                                .contentType("application/json")
                                .body(registerBody.toString())
                                .post("/api/v1/auth/register")
                                .then()
                                .statusCode(201)
                                .extract()
                                .response();

                Long userId = createdUser.jsonPath().getLong("id");

                given()
                                .header("Cookie", cookieHeader)
                                .when()
                                .put("/api/v1/users/" + userId + "/ban")
                                .then()
                                .statusCode(200)
                                .body("banned", equalTo(true));

                JSONObject loginBody = new JSONObject();
                loginBody.put("username", "BannedUser");
                loginBody.put("password", "password123");

                given()
                                .contentType("application/json")
                                .body(loginBody.toString())
                                .when()
                                .post("/api/v1/auth/login")
                                .then()
                                .statusCode(403)
                                .body("status", equalTo("FAILURE"))
                                .body("message", equalTo("No puedes iniciar sesion: tu cuenta esta baneada."));
        }

}
