package es.codeurjc.quesosbartolome.system;

import io.restassured.RestAssured;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "spring.profiles.active=test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class ApiUserTests {

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

        private io.restassured.http.Cookies registerAndLoginTestUser(String name, String password) throws JSONException {
                String uniqueNif = String.format("%08d", Math.abs(name.hashCode() % 100000000)) + "Z";

                JSONObject registerBody = new JSONObject();
                registerBody.put("name", name);
                registerBody.put("password", password);
                registerBody.put("gmail", name.toLowerCase() + "@example.com");
                registerBody.put("direction", "Street of " + name);
                registerBody.put("nif", uniqueNif);
                registerBody.put("image", JSONObject.NULL);

                given()
                                .contentType("application/json")
                                .body(registerBody.toString())
                                .post("/api/v1/auth/register")
                                .then()
                                .statusCode(anyOf(is(201)));

                return login(name, password);
        }

        private io.restassured.http.Cookies loginAsAdmin() throws JSONException {
                return login("Admin", "password123");
        }

        private Long getCurrentUserId(io.restassured.http.Cookies cookies) {
                Number id = given()
                                .cookies(cookies)
                                .when()
                                .get("/api/v1/users")
                                .then()
                                .statusCode(200)
                                .extract()
                                .path("id");
                return id.longValue();
        }

        private Long createOrderAsUser(io.restassured.http.Cookies userCookies) {
                given()
                                .cookies(userCookies)
                                .queryParam("cheeseId", 5)
                                .queryParam("boxes", 1)
                                .when()
                                .put("/api/v1/cart/addItem")
                                .then()
                                .statusCode(200);

                Integer confirmedOrderId = given()
                                .cookies(userCookies)
                                .when()
                                .post("/api/v1/orders/confirm")
                                .then()
                                .statusCode(201)
                                .extract()
                                .path("id");

                if (confirmedOrderId == null) {
                        throw new AssertionError("Confirmed order id should not be null");
                }

                return confirmedOrderId.longValue();
        }

        private Long createInvoiceAsAdmin(io.restassured.http.Cookies adminCookies, Long orderId) throws JSONException {
                JSONObject body = new JSONObject();
                body.put("id", orderId);

                Integer invoiceId = given()
                                .cookies(adminCookies)
                                .contentType("application/json")
                                .body(body.toString())
                                .when()
                                .post("/api/v1/invoices")
                                .then()
                                .statusCode(anyOf(is(200), is(201)))
                                .extract()
                                .path("id");

                if (invoiceId == null) {
                        throw new AssertionError("Invoice id should not be null");
                }

                return invoiceId.longValue();
        }

        @Test
        @Order(1)
        void testGetCurrentUserUnauthorized() {
                given()
                                .when()
                                .get("/api/v1/users")
                                .then()
                                .statusCode(401);
        }

        @Test
        @Order(2)
        void testGetCurrentUserAfterLogin() throws JSONException {
                var cookies = registerAndLoginTestUser("UserTest1", "password123");

                given()
                                .cookies(cookies)
                                .when()
                                .get("/api/v1/users")
                                .then()
                                .statusCode(200)
                                .body("name", equalTo("UserTest1"))
                                .body("gmail", equalTo("usertest1@example.com"))
                                .body("direction", equalTo("Street of UserTest1"))
                                .body("id", notNullValue())
                                .body("password", notNullValue());
        }

        @Test
        @Order(3)
        void testGetUserImageById() throws JSONException {
                var cookies = registerAndLoginTestUser("UserTest2", "password123");
                Long userId = getCurrentUserId(cookies);

                given()
                                .when()
                                .get("/api/v1/users/" + userId + "/image")
                                .then()
                                .statusCode(200);
        }

        @Test
        @Order(4)
        void testGetUserImageByIdNotFound() throws JSONException {
                Long userId = 99999L;

                given()
                                .when()
                                .get("/api/v1/users/" + userId + "/image")
                                .then()
                                .statusCode(404);
        }

        @Test
        @Order(4)
        void testGetUserById() throws JSONException {
                var cookies = registerAndLoginTestUser("UserTest3", "password123");
                Long userId = getCurrentUserId(cookies);

                given()
                                .when()
                                .get("/api/v1/users/" + userId)
                                .then()
                                .statusCode(200)
                                .body("name", equalTo("UserTest3"))
                                .body("gmail", equalTo("usertest3@example.com"))
                                .body("direction", equalTo("Street of UserTest3"))
                                .body("id", equalTo(userId.intValue()));
        }

        @Test
        @Order(6)
        void testGetUserByIdNotFound() {
                given()
                                .when()
                                .get("/api/v1/users/99999")
                                .then()
                                .statusCode(404);
        }

        @Test
        @Order(7)
        void testGetAllUsersUnauthorized() {
                given()
                                .when()
                                .get("/api/v1/users/all")
                                .then()
                                .statusCode(401);
        }

        @Test
        @Order(8)
        void testGetAllUsersForbiddenForUser() throws JSONException {
                var cookies = registerAndLoginTestUser("UserTest4", "password123");

                given()
                                .cookies(cookies)
                                .when()
                                .get("/api/v1/users/all")
                                .then()
                                .statusCode(403);
        }

        @Test
        @Order(9)
        void testGetAllUsersAsAdmin() throws JSONException {
                var cookies = loginAsAdmin();

                given()
                                .cookies(cookies)
                                .when()
                                .get("/api/v1/users/all")
                                .then()
                                .statusCode(200)
                                .body("content", notNullValue())
                                .body("totalElements", greaterThanOrEqualTo(0));
        }

        @Test
        @Order(10)
        void testGetAllUsersWithPaginationAsAdmin() throws JSONException {
                var cookies = loginAsAdmin();

                given()
                                .cookies(cookies)
                                .queryParam("page", 0)
                                .queryParam("size", 2)
                                .when()
                                .get("/api/v1/users/all")
                                .then()
                                .statusCode(200)
                                .body("content.size()", lessThanOrEqualTo(2))
                                .body("size", equalTo(2));
        }

        @Test
        @Order(11)
        void testUpdateUserUnauthorized() throws JSONException {
                JSONObject body = new JSONObject();
                body.put("name", "NewName");

                given()
                                .contentType("application/json")
                                .body(body.toString())
                                .when()
                                .put("/api/v1/users/1")
                                .then()
                                .statusCode(401);
        }

        @Test
        @Order(12)
        void testUpdateUserForbidden() throws JSONException {
                var cookiesA = registerAndLoginTestUser("UserTest5", "password123");
                Long idA = getCurrentUserId(cookiesA);

                var cookiesB = registerAndLoginTestUser("UserTest6", "password123");

                JSONObject update = new JSONObject();
                update.put("name", "HackedName");

                given()
                                .cookies(cookiesB)
                                .contentType("application/json")
                                .body(update.toString())
                                .when()
                                .put("/api/v1/users/" + idA)
                                .then()
                                .statusCode(403);
        }

        @Test
        @Order(13)
        void testUpdateUserSuccess() throws JSONException {
                var cookies = registerAndLoginTestUser("UserTest7", "password123");
                Long id = getCurrentUserId(cookies);

                JSONObject update = new JSONObject();
                update.put("name", "UpdatedName");
                update.put("gmail", "newmail@test.com");
                update.put("direction", "New Street");
                update.put("nif", "44444444D");

                given()
                                .cookies(cookies)
                                .contentType("application/json")
                                .body(update.toString())
                                .when()
                                .put("/api/v1/users/" + id)
                                .then()
                                .statusCode(200)
                                .body("name", equalTo("UpdatedName"))
                                .body("gmail", equalTo("newmail@test.com"))
                                .body("direction", equalTo("New Street"))
                                .body("nif", equalTo("44444444D"));
        }

        @Test
        @Order(14)
        void testUpdateUserNameChangeRefreshesCookies() throws JSONException {
                var cookies = registerAndLoginTestUser("UserTest8", "password123");
                Long id = getCurrentUserId(cookies);

                JSONObject update = new JSONObject();
                update.put("name", "UserTest8Updated");
                update.put("gmail", "usertest8updated@example.com");
                update.put("direction", "New Street");
                update.put("nif", "55555555B");

                var response = given()
                                .cookies(cookies)
                                .contentType("application/json")
                                .body(update.toString())
                                .when()
                                .put("/api/v1/users/" + id)
                                .then()
                                .statusCode(200)
                                .extract()
                                .response();

                List<String> cookieHeaders = response.getHeaders().getValues("Set-Cookie");
                assertFalse(cookieHeaders.isEmpty(), "Renaming user should return refreshed cookies");

                String cookieHeader = cookieHeaders.stream()
                                .map(setCookie -> setCookie.split(";", 2)[0])
                                .reduce((c1, c2) -> c1 + "; " + c2)
                                .orElse("");

                assertTrue(cookieHeader.contains("AuthToken="), "Access token cookie should be refreshed");
                assertTrue(cookieHeader.contains("RefreshToken="), "Refresh token cookie should be refreshed");
        }

        @Test
        @Order(14)
        void testUpdateUserImageUnauthorized() {
                given()
                                .multiPart("file", "photo.png", "fake".getBytes())
                                .when()
                                .put("/api/v1/users/1/image")
                                .then()
                                .statusCode(401);
        }

        @Test
        @Order(15)
        void testUpdateUserImageForbidden() throws JSONException {
                var cookiesA = registerAndLoginTestUser("UserTest9", "password123");
                Long idA = getCurrentUserId(cookiesA);

                var cookiesB = registerAndLoginTestUser("UserTest10", "password123");

                given()
                                .cookies(cookiesB)
                                .multiPart("file", "photo.png", "fake".getBytes())
                                .when()
                                .put("/api/v1/users/" + idA + "/image")
                                .then()
                                .statusCode(403);
        }

        @Test
        @Order(16)
        void testUpdateUserImageSuccess() throws JSONException {
                var cookies = registerAndLoginTestUser("UserTest11", "password123");
                Long id = getCurrentUserId(cookies);

                given()
                                .cookies(cookies)
                                .multiPart("file", "photo.png", "fakeImageData".getBytes())
                                .when()
                                .put("/api/v1/users/" + id + "/image")
                                .then()
                                .statusCode(200);
        }

        @Test
        @Order(17)
        void testChangePasswordUnauthorized() throws JSONException {
                JSONObject body = new JSONObject();
                body.put("currentPassword", "old");
                body.put("newPassword", "newPassword123");
                body.put("confirmPassword", "newPassword123");

                given()
                                .contentType("application/json")
                                .body(body.toString())
                                .when()
                                .put("/api/v1/users/1/password")
                                .then()
                                .statusCode(401);
        }

        @Test
        @Order(18)
        void testChangePasswordForbidden() throws JSONException {
                var cookiesA = registerAndLoginTestUser("UserTest12", "password123");
                Long idA = getCurrentUserId(cookiesA);

                var cookiesB = registerAndLoginTestUser("UserTest13", "password123");

                JSONObject body = new JSONObject();
                body.put("currentPassword", "password123");
                body.put("newPassword", "newPass123");
                body.put("confirmPassword", "newPass123");

                given()
                                .cookies(cookiesB)
                                .contentType("application/json")
                                .body(body.toString())
                                .when()
                                .put("/api/v1/users/" + idA + "/password")
                                .then()
                                .statusCode(403);
        }

        @Test
        @Order(19)
        void testChangePasswordWrongCurrentPassword() throws JSONException {
                var cookies = registerAndLoginTestUser("UserTest14", "correctPwd");
                Long id = getCurrentUserId(cookies);

                JSONObject body = new JSONObject();
                body.put("currentPassword", "wrongPwd");
                body.put("newPassword", "newPassword123");
                body.put("confirmPassword", "newPassword123");

                given()
                                .cookies(cookies)
                                .contentType("application/json")
                                .body(body.toString())
                                .when()
                                .put("/api/v1/users/" + id + "/password")
                                .then()
                                .statusCode(400);
        }

        @Test
        @Order(20)
        void testChangePasswordSuccess() throws JSONException {
                var cookies = registerAndLoginTestUser("UserTest15", "oldPassword");
                Long id = getCurrentUserId(cookies);

                JSONObject body = new JSONObject();
                body.put("currentPassword", "oldPassword");
                body.put("newPassword", "newPassword123");
                body.put("confirmPassword", "newPassword123");

                given()
                                .cookies(cookies)
                                .contentType("application/json")
                                .body(body.toString())
                                .when()
                                .put("/api/v1/users/" + id + "/password")
                                .then()
                                .statusCode(200);
        }

        @Test
        @Order(21)
        void testBanUserUnauthorized() {
                given()
                                .when()
                                .put("/api/v1/users/1/ban")
                                .then()
                                .statusCode(401);
        }

        @Test
        @Order(22)
        void testBanUserForbiddenForNonAdmin() throws JSONException {
                var cookies = registerAndLoginTestUser("UserTest16", "password123");

                given()
                                .cookies(cookies)
                                .when()
                                .put("/api/v1/users/999/ban")
                                .then()
                                .statusCode(403);
        }

        @Test
        @Order(23)
        void testBanUserNotFound() throws JSONException {
                var cookies = loginAsAdmin();

                given()
                                .cookies(cookies)
                                .when()
                                .put("/api/v1/users/999999/ban")
                                .then()
                                .statusCode(404);
        }

        @Test
        @Order(24)
        void testBanUserSuccess() throws JSONException {
                var adminCookies = loginAsAdmin();
                var userCookies = registerAndLoginTestUser("UserTest17", "password123");
                Long id = getCurrentUserId(userCookies);

                given()
                                .cookies(adminCookies)
                                .when()
                                .put("/api/v1/users/" + id + "/ban")
                                .then()
                                .statusCode(200)
                                .body("banned", equalTo(true));
        }

        @Test
        @Order(25)
        void testUnbanUserSuccess() throws JSONException {
                var adminCookies = loginAsAdmin();
                var userCookies = registerAndLoginTestUser("UserTest18", "password123");
                Long id = getCurrentUserId(userCookies);

                given()
                                .cookies(adminCookies)
                                .when()
                                .put("/api/v1/users/" + id + "/ban")
                                .then()
                                .statusCode(200)
                                .body("banned", equalTo(true));

                given()
                                .cookies(adminCookies)
                                .when()
                                .put("/api/v1/users/" + id + "/ban")
                                .then()
                                .statusCode(200)
                                .body("banned", equalTo(false));
        }

        @Test
        @Order(26)
        void testGetMyOrdersUnauthorized() {
                given()
                                .when()
                                .get("/api/v1/users/1/orders")
                                .then()
                                .statusCode(401);
        }

        @Test
        @Order(27)
        void testGetMyOrdersAsUserSuccess() throws JSONException {
                var userCookies = registerAndLoginTestUser("UserTest19", "password123");
                Long userId = getCurrentUserId(userCookies);

                given()
                                .cookies(userCookies)
                                .queryParam("page", 0)
                                .queryParam("size", 10)
                                .when()
                                .get("/api/v1/users/" + userId + "/orders")
                                .then()
                                .statusCode(200)
                                .body("content", notNullValue());
        }

        @Test
        @Order(28)
        void testGetMyOrdersForbiddenWhenUserUsesAdminId() throws JSONException {
                var userCookies = registerAndLoginTestUser("UserTest20", "password123");
                var adminCookies = loginAsAdmin();
                Long adminId = getCurrentUserId(adminCookies);

                given()
                                .cookies(userCookies)
                                .when()
                                .get("/api/v1/users/" + adminId + "/orders")
                                .then()
                                .statusCode(403);
        }

        @Test
        @Order(29)
        void testGetMyOrdersForbiddenWhenAdminUsesUserId() throws JSONException {
                var userCookies = registerAndLoginTestUser("UserTest21", "password123");
                var adminCookies = loginAsAdmin();
                Long userId = getCurrentUserId(userCookies);

                given()
                                .cookies(adminCookies)
                                .when()
                                .get("/api/v1/users/" + userId + "/orders")
                                .then()
                                .statusCode(403);
        }

        @Test
        @Order(30)
        void testGetMyOrderByIdUnauthorized() {
                given()
                                .when()
                                .get("/api/v1/users/1/orders/1")
                                .then()
                                .statusCode(401);
        }

        @Test
        @Order(31)
        void testGetMyOrderByIdNotFoundForUser() throws JSONException {
                var userCookies = registerAndLoginTestUser("UserTest22", "password123");
                Long userId = getCurrentUserId(userCookies);

                given()
                                .cookies(userCookies)
                                .when()
                                .get("/api/v1/users/" + userId + "/orders/999999")
                                .then()
                                .statusCode(404);
        }

        @Test
        @Order(32)
        void testGetMyInvoicesUnauthorized() {
                given()
                                .when()
                                .get("/api/v1/users/1/invoices")
                                .then()
                                .statusCode(401);
        }

        @Test
        @Order(33)
        void testGetMyInvoicesAsUserSuccess() throws JSONException {
                var userCookies = registerAndLoginTestUser("UserTest23", "password123");
                Long userId = getCurrentUserId(userCookies);

                given()
                                .cookies(userCookies)
                                .queryParam("page", 0)
                                .queryParam("size", 10)
                                .when()
                                .get("/api/v1/users/" + userId + "/invoices")
                                .then()
                                .statusCode(200)
                                .body("content", notNullValue());
        }

        @Test
        @Order(34)
        void testGetMyInvoicesForbiddenWhenUserUsesAdminId() throws JSONException {
                var userCookies = registerAndLoginTestUser("UserTest24", "password123");
                var adminCookies = loginAsAdmin();
                Long adminId = getCurrentUserId(adminCookies);

                given()
                                .cookies(userCookies)
                                .when()
                                .get("/api/v1/users/" + adminId + "/invoices")
                                .then()
                                .statusCode(403);
        }

        @Test
        @Order(37)
        void testDownloadMyInvoicePdfUnauthorized() {
                given()
                                .when()
                                .get("/api/v1/users/1/invoices/1/download-pdf")
                                .then()
                                .statusCode(401);
        }

        @Test
        @Order(38)
        void testDownloadMyInvoicePdfForbiddenWhenUserUsesAdminId() throws JSONException {
                var userCookies = registerAndLoginTestUser("UserTest25", "password123");
                var adminCookies = loginAsAdmin();
                Long adminId = getCurrentUserId(adminCookies);

                given()
                                .cookies(userCookies)
                                .when()
                                .get("/api/v1/users/" + adminId + "/invoices/1/download-pdf")
                                .then()
                                .statusCode(403);
        }

        @Test
        @Order(39)
        void testDownloadMyInvoicePdfNotFoundForUser() throws JSONException {
                var userCookies = registerAndLoginTestUser("UserTest26", "password123");
                Long userId = getCurrentUserId(userCookies);

                given()
                                .cookies(userCookies)
                                .when()
                                .get("/api/v1/users/" + userId + "/invoices/999999/download-pdf")
                                .then()
                                .statusCode(404);
        }

        @Test
        @Order(40)
        void testGetMyOrderByIdSuccessForUser() throws JSONException {
                var userCookies = registerAndLoginTestUser("UserTest27", "password123");
                Long userId = getCurrentUserId(userCookies);
                Long orderId = createOrderAsUser(userCookies);

                given()
                                .cookies(userCookies)
                                .when()
                                .get("/api/v1/users/" + userId + "/orders/" + orderId)
                                .then()
                                .statusCode(200)
                                .body("id", equalTo(orderId.intValue()))
                                .body("user.name", equalTo("UserTest27"));
        }

        @Test
        @Order(42)
        void testDownloadMyInvoicePdfSuccessForUser() throws JSONException {
                var userCookies = registerAndLoginTestUser("UserTest28", "password123");
                var adminCookies = loginAsAdmin();

                Long userId = getCurrentUserId(userCookies);
                Long orderId = createOrderAsUser(userCookies);
                Long invoiceId = createInvoiceAsAdmin(adminCookies, orderId);

                given()
                                .cookies(userCookies)
                                .when()
                                .get("/api/v1/users/" + userId + "/invoices/" + invoiceId + "/download-pdf")
                                .then()
                                .statusCode(200)
                                .header("Content-Type", containsString("application/pdf"));
        }

        @Test
        @Order(43)
        void testAllUsers_Unauthorized() {
                given()
                                .when()
                                .get("/api/v1/users/allUsers")
                                .then()
                                .statusCode(401);
        }

        @Test
        @Order(44)
        void testAllUsers_Forbidden_ForNormalUser() throws JSONException {
                var cookies = registerAndLoginTestUser("UserTest29", "password123");

                given()
                                .cookies(cookies)
                                .when()
                                .get("/api/v1/users/allUsers")
                                .then()
                                .statusCode(403);
        }

        @Test
        @Order(45)
        void testAllUsers_AsAdmin() throws JSONException {
                var cookies = loginAsAdmin();

                given()
                                .cookies(cookies)
                                .when()
                                .get("/api/v1/users/allUsers")
                                .then()
                                .statusCode(200)
                                .body("$", notNullValue());
        }

        @Test
        @Order(46)
        void testAllUsers_AsAdmin_ListContainsUsers() throws JSONException {
                var cookies = loginAsAdmin();

                given()
                                .cookies(cookies)
                                .when()
                                .get("/api/v1/users/allUsers")
                                .then()
                                .statusCode(200)
                                .body("size()", greaterThanOrEqualTo(1))
                                .body("[0].id", notNullValue());
        }

}
