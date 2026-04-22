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

        private io.restassured.http.Cookies loginAsAdminGerman() throws JSONException {
                return login("German", "password123");
        }

        private io.restassured.http.Cookies loginAsUserVictor() throws JSONException {
                return login("Victor", "password123");
        }

        @Test
        @Order(1)
        void testGetCurrentUserUnauthorized() {
                // Without login, should return 401 Unauthorized
                given()
                                .when()
                                .get("/api/v1/users")
                                .then()
                                .statusCode(401);
        }

        @Test
        @Order(2)
        void testGetCurrentUserAfterLogin() throws JSONException {
                // Register test user
                JSONObject registerBody = new JSONObject();
                registerBody.put("name", "JorgeTestUser");
                registerBody.put("password", "password123");
                registerBody.put("gmail", "jorge.test@example.com");
                registerBody.put("direction", "Calle Victoria 1");
                registerBody.put("nif", "87654321B");
                registerBody.put("image", JSONObject.NULL);

                given()
                                .contentType("application/json")
                                .body(registerBody.toString())
                                .post("/api/v1/auth/register")
                                .then()
                                .statusCode(201); // Verify registration was successful

                // Login
                JSONObject loginBody = new JSONObject();
                loginBody.put("username", "JorgeTestUser");
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
                                .body("name", equalTo("JorgeTestUser"))
                                .body("gmail", equalTo("jorge.test@example.com"))
                                .body("direction", equalTo("Calle Victoria 1"))
                                .body("nif", equalTo("87654321B"))
                                .body("id", notNullValue())
                                .body("password", notNullValue());
        }

        @Test
        @Order(3)
        void testGetUserImageById() throws JSONException {
                JSONObject registerBody = new JSONObject();
                registerBody.put("name", "AnaTestUser");
                registerBody.put("password", "clave456");
                registerBody.put("gmail", "ana.test@example.com");
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
                                .statusCode(200);
        }

        @Test
        @Order(4)
        void testGetUserImageByIdNotFound() throws JSONException {
                Long userId = 99999L; // Assuming this ID does not exist

                given()
                                .when()
                                .get("/api/v1/users/" + userId + "/image")
                                .then()
                                .statusCode(404);
        }

        @Test
        @Order(5)
        void testGetUserById() throws JSONException {
                // Registrar un usuario
                JSONObject registerBody = new JSONObject();
                registerBody.put("name", "LuisTestUser");
                registerBody.put("password", "password789");
                registerBody.put("gmail", "luis.test@example.com");
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
                                .body("name", equalTo("LuisTestUser"))
                                .body("gmail", equalTo("luis.test@example.com"))
                                .body("direction", equalTo("Calle Mayor 3"))
                                .body("nif", equalTo("11223344D"))
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
                // Register USER
                JSONObject registerBody = new JSONObject();
                registerBody.put("name", "NormalTestUser");
                registerBody.put("password", "password");
                registerBody.put("gmail", "normal.test@example.com");
                registerBody.put("direction", "Calle Normal");
                registerBody.put("nif", "11111111A");
                registerBody.put("image", JSONObject.NULL);

                given()
                                .contentType("application/json")
                                .body(registerBody.toString())
                                .post("/api/v1/auth/register")
                                .then()
                                .statusCode(201);

                // Login USER
                JSONObject loginBody = new JSONObject();
                loginBody.put("username", "NormalTestUser");
                loginBody.put("password", "password");

                var cookies = given()
                                .contentType("application/json")
                                .body(loginBody.toString())
                                .post("/api/v1/auth/login")
                                .then()
                                .statusCode(200)
                                .extract()
                                .detailedCookies();

                // USER tries to access ADMIN endpoint
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

                // Login as ADMIN (German is admin by default)
                JSONObject loginBody = new JSONObject();
                loginBody.put("username", "German");
                loginBody.put("password", "password123");

                var cookies = given()
                                .contentType("application/json")
                                .body(loginBody.toString())
                                .post("/api/v1/auth/login")
                                .then()
                                .statusCode(200)
                                .extract()
                                .detailedCookies();

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

                // Login as ADMIN
                JSONObject loginBody = new JSONObject();
                loginBody.put("username", "German");
                loginBody.put("password", "password123");

                var cookies = given()
                                .contentType("application/json")
                                .body(loginBody.toString())
                                .post("/api/v1/auth/login")
                                .then()
                                .statusCode(200)
                                .extract()
                                .detailedCookies();

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

                // Register A
                JSONObject regA = new JSONObject();
                regA.put("name", "UserA");
                regA.put("password", "passwordA");
                regA.put("gmail", "a@test.com");
                regA.put("direction", "Street A");
                regA.put("nif", "11111111A");
                regA.put("image", JSONObject.NULL);

                var resA = given()
                                .contentType("application/json")
                                .body(regA.toString())
                                .post("/api/v1/auth/register")
                                .then()
                                .statusCode(201)
                                .extract()
                                .response();

                Number idANum = resA.path("id");
                Long idA = idANum.longValue();

                // Register B
                JSONObject regB = new JSONObject();
                regB.put("name", "UserB");
                regB.put("password", "passwordB");
                regB.put("gmail", "b@test.com");
                regB.put("direction", "Street B");
                regB.put("nif", "22222222B");
                regB.put("image", JSONObject.NULL);

                given()
                                .contentType("application/json")
                                .body(regB.toString())
                                .post("/api/v1/auth/register")
                                .then()
                                .statusCode(201);

                // Login B
                JSONObject loginB = new JSONObject();
                loginB.put("username", "UserB");
                loginB.put("password", "passwordB");

                var cookiesB = given()
                                .contentType("application/json")
                                .body(loginB.toString())
                                .post("/api/v1/auth/login")
                                .then()
                                .statusCode(200)
                                .extract()
                                .detailedCookies();

                // B tries to update A → forbidden
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

                JSONObject reg = new JSONObject();
                reg.put("name", "UpdateUser");
                reg.put("password", "password123");
                reg.put("gmail", "update@test.com");
                reg.put("direction", "Old Street");
                reg.put("nif", "33333333C");
                reg.put("image", JSONObject.NULL);

                var res = given()
                                .contentType("application/json")
                                .body(reg.toString())
                                .post("/api/v1/auth/register")
                                .then()
                                .statusCode(201)
                                .extract()
                                .response();

                Number idNum = res.path("id");
                Long id = idNum.longValue();

                JSONObject login = new JSONObject();
                login.put("username", "UpdateUser");
                login.put("password", "password123");

                var cookies = given()
                                .contentType("application/json")
                                .body(login.toString())
                                .post("/api/v1/auth/login")
                                .then()
                                .statusCode(200)
                                .extract()
                                .detailedCookies();

                // Update
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

                JSONObject regA = new JSONObject();
                regA.put("name", "ImgA");
                regA.put("password", "pwdA1234");
                regA.put("gmail", "a@img.com");
                regA.put("direction", "Street A");
                regA.put("nif", "11111111A");
                regA.put("image", JSONObject.NULL);

                var resA = given()
                                .contentType("application/json")
                                .body(regA.toString())
                                .post("/api/v1/auth/register")
                                .then()
                                .statusCode(201)
                                .extract()
                                .response();

                Number idANum = resA.path("id");
                Long idA = idANum.longValue();

                JSONObject regB = new JSONObject();
                regB.put("name", "ImgB");
                regB.put("password", "pwdB1234");
                regB.put("gmail", "b@img.com");
                regB.put("direction", "Street B");
                regB.put("nif", "22222222B");
                regB.put("image", JSONObject.NULL);

                given()
                                .contentType("application/json")
                                .body(regB.toString())
                                .post("/api/v1/auth/register")
                                .then()
                                .statusCode(201);

                JSONObject loginB = new JSONObject();
                loginB.put("username", "ImgB");
                loginB.put("password", "pwdB1234");

                var cookiesB = given()
                                .contentType("application/json")
                                .body(loginB.toString())
                                .post("/api/v1/auth/login")
                                .then()
                                .statusCode(200)
                                .extract()
                                .detailedCookies();

                // B tries to upload image for A → forbidden
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

                JSONObject reg = new JSONObject();
                reg.put("name", "ImgUser");
                reg.put("password", "pwd12345");
                reg.put("gmail", "img@test.com");
                reg.put("direction", "Street Img");
                reg.put("nif", "55555555A");
                reg.put("image", JSONObject.NULL);

                var res = given()
                                .contentType("application/json")
                                .body(reg.toString())
                                .post("/api/v1/auth/register")
                                .then()
                                .statusCode(201)
                                .extract()
                                .response();

                Number idNum = res.path("id");
                Long id = idNum.longValue();

                JSONObject login = new JSONObject();
                login.put("username", "ImgUser");
                login.put("password", "pwd12345");

                var cookies = given()
                                .contentType("application/json")
                                .body(login.toString())
                                .post("/api/v1/auth/login")
                                .then()
                                .statusCode(200)
                                .extract()
                                .detailedCookies();

                // Upload image
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

                // Register A
                JSONObject regA = new JSONObject();
                regA.put("name", "PassA");
                regA.put("password", "pwdA1234");
                regA.put("gmail", "a@pass.com");
                regA.put("direction", "Street A");
                regA.put("nif", "11111111A");
                regA.put("image", JSONObject.NULL);

                var resA = given()
                                .contentType("application/json")
                                .body(regA.toString())
                                .post("/api/v1/auth/register")
                                .then()
                                .statusCode(201)
                                .extract()
                                .response();

                Number idANum = resA.path("id");
                Long idA = idANum.longValue();

                // Register B
                JSONObject regB = new JSONObject();
                regB.put("name", "PassB");
                regB.put("password", "pwdB1234");
                regB.put("gmail", "b@pass.com");
                regB.put("direction", "Street B");
                regB.put("nif", "22222222B");
                regB.put("image", JSONObject.NULL);

                given()
                                .contentType("application/json")
                                .body(regB.toString())
                                .post("/api/v1/auth/register")
                                .then()
                                .statusCode(201);

                // Login B
                JSONObject loginB = new JSONObject();
                loginB.put("username", "PassB");
                loginB.put("password", "pwdB1234");

                var cookiesB = given()
                                .contentType("application/json")
                                .body(loginB.toString())
                                .post("/api/v1/auth/login")
                                .then()
                                .statusCode(200)
                                .extract()
                                .detailedCookies();

                // B tries to change password of A → forbidden
                JSONObject body = new JSONObject();
                body.put("currentPassword", "pwdA1234");
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

                JSONObject reg = new JSONObject();
                reg.put("name", "WrongPassUser");
                reg.put("password", "correctPwd");
                reg.put("gmail", "wrongpass@test.com");
                reg.put("direction", "Street");
                reg.put("nif", "99999999X");
                reg.put("image", JSONObject.NULL);

                var res = given()
                                .contentType("application/json")
                                .body(reg.toString())
                                .post("/api/v1/auth/register")
                                .then()
                                .statusCode(201)
                                .extract()
                                .response();

                Number idNum = res.path("id");
                Long id = idNum.longValue();

                JSONObject login = new JSONObject();
                login.put("username", "WrongPassUser");
                login.put("password", "correctPwd");

                var cookies = given()
                                .contentType("application/json")
                                .body(login.toString())
                                .post("/api/v1/auth/login")
                                .then()
                                .statusCode(200)
                                .extract()
                                .detailedCookies();

                // Wrong current password
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

                JSONObject reg = new JSONObject();
                reg.put("name", "PassSuccess");
                reg.put("password", "oldPassword");
                reg.put("gmail", "passsuccess@test.com");
                reg.put("direction", "Street");
                reg.put("nif", "12312312Z");
                reg.put("image", JSONObject.NULL);

                var res = given()
                                .contentType("application/json")
                                .body(reg.toString())
                                .post("/api/v1/auth/register")
                                .then()
                                .statusCode(201)
                                .extract()
                                .response();

                Number idNum = res.path("id");
                Long id = idNum.longValue();

                JSONObject login = new JSONObject();
                login.put("username", "PassSuccess");
                login.put("password", "oldPassword");

                var cookies = given()
                                .contentType("application/json")
                                .body(login.toString())
                                .post("/api/v1/auth/login")
                                .then()
                                .statusCode(200)
                                .extract()
                                .detailedCookies();

                // Change password
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

                JSONObject reg = new JSONObject();
                reg.put("name", "NonAdminUser");
                reg.put("password", "password123");
                reg.put("gmail", "nonadmin@test.com");
                reg.put("direction", "Street");
                reg.put("nif", "12345678Z");
                reg.put("image", JSONObject.NULL);

                given()
                                .contentType("application/json")
                                .body(reg.toString())
                                .post("/api/v1/auth/register")
                                .then()
                                .statusCode(201);

                JSONObject login = new JSONObject();
                login.put("username", "NonAdminUser");
                login.put("password", "password123");

                var cookies = given()
                                .contentType("application/json")
                                .body(login.toString())
                                .post("/api/v1/auth/login")
                                .then()
                                .statusCode(200)
                                .extract()
                                .detailedCookies();

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

                JSONObject login = new JSONObject();
                login.put("username", "German");
                login.put("password", "password123");

                var cookies = given()
                                .contentType("application/json")
                                .body(login.toString())
                                .post("/api/v1/auth/login")
                                .then()
                                .statusCode(200)
                                .extract()
                                .detailedCookies();

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

                JSONObject login = new JSONObject();
                login.put("username", "German");
                login.put("password", "password123");

                var cookies = given()
                                .contentType("application/json")
                                .body(login.toString())
                                .post("/api/v1/auth/login")
                                .then()
                                .statusCode(200)
                                .extract()
                                .detailedCookies();

                JSONObject reg = new JSONObject();
                reg.put("name", "BanTarget");
                reg.put("password", "password123");
                reg.put("gmail", "ban@test.com");
                reg.put("direction", "Street");
                reg.put("nif", "87654321Z");
                reg.put("image", JSONObject.NULL);

                var res = given()
                                .contentType("application/json")
                                .body(reg.toString())
                                .post("/api/v1/auth/register")
                                .then()
                                .statusCode(201)
                                .extract()
                                .response();

                Long id = res.jsonPath().getLong("id");

                given()
                                .cookies(cookies)
                                .when()
                                .put("/api/v1/users/" + id + "/ban")
                                .then()
                                .statusCode(200)
                                .body("banned", equalTo(true));
        }

        @Test
        @Order(25)
        void testUnbanUserSuccess() throws JSONException {

                JSONObject login = new JSONObject();
                login.put("username", "German");
                login.put("password", "password123");

                var cookies = given()
                                .contentType("application/json")
                                .body(login.toString())
                                .post("/api/v1/auth/login")
                                .then()
                                .statusCode(200)
                                .extract()
                                .detailedCookies();

                JSONObject reg = new JSONObject();
                reg.put("name", "UnbanTarget");
                reg.put("password", "password123");
                reg.put("gmail", "unban@test.com");
                reg.put("direction", "Street");
                reg.put("nif", "11223344Z");
                reg.put("image", JSONObject.NULL);

                var res = given()
                                .contentType("application/json")
                                .body(reg.toString())
                                .post("/api/v1/auth/register")
                                .then()
                                .statusCode(201)
                                .extract()
                                .response();

                Long id = res.jsonPath().getLong("id");

                given()
                                .cookies(cookies)
                                .when()
                                .put("/api/v1/users/" + id + "/ban")
                                .then()
                                .statusCode(200)
                                .body("banned", equalTo(true));

                given()
                                .cookies(cookies)
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
        void testGetMyOrdersAsVictorSuccess() throws JSONException {
                var victorCookies = loginAsUserVictor();
                Long victorId = getCurrentUserId(victorCookies);

                given()
                                .cookies(victorCookies)
                                .queryParam("page", 0)
                                .queryParam("size", 10)
                                .when()
                                .get("/api/v1/users/" + victorId + "/orders")
                                .then()
                                .statusCode(200)
                                .body("content", notNullValue());
        }

        @Test
        @Order(28)
        void testGetMyOrdersForbiddenWhenVictorUsesGermanId() throws JSONException {
                var victorCookies = loginAsUserVictor();
                var germanCookies = loginAsAdminGerman();

                Long germanId = getCurrentUserId(germanCookies);

                given()
                                .cookies(victorCookies)
                                .when()
                                .get("/api/v1/users/" + germanId + "/orders")
                                .then()
                                .statusCode(403);
        }

        @Test
        @Order(29)
        void testGetMyOrdersForbiddenWhenGermanUsesVictorId() throws JSONException {
                var victorCookies = loginAsUserVictor();
                var germanCookies = loginAsAdminGerman();

                Long victorId = getCurrentUserId(victorCookies);

                given()
                                .cookies(germanCookies)
                                .when()
                                .get("/api/v1/users/" + victorId + "/orders")
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
        void testGetMyOrderByIdNotFoundForVictor() throws JSONException {
                var victorCookies = loginAsUserVictor();
                Long victorId = getCurrentUserId(victorCookies);

                given()
                                .cookies(victorCookies)
                                .when()
                                .get("/api/v1/users/" + victorId + "/orders/999999")
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
        void testGetMyInvoicesAsVictorSuccess() throws JSONException {
                var victorCookies = loginAsUserVictor();
                Long victorId = getCurrentUserId(victorCookies);

                given()
                                .cookies(victorCookies)
                                .queryParam("page", 0)
                                .queryParam("size", 10)
                                .when()
                                .get("/api/v1/users/" + victorId + "/invoices")
                                .then()
                                .statusCode(200)
                                .body("content", notNullValue());
        }

        @Test
        @Order(34)
        void testGetMyInvoicesForbiddenWhenVictorUsesGermanId() throws JSONException {
                var victorCookies = loginAsUserVictor();
                var germanCookies = loginAsAdminGerman();

                Long germanId = getCurrentUserId(germanCookies);

                given()
                                .cookies(victorCookies)
                                .when()
                                .get("/api/v1/users/" + germanId + "/invoices")
                                .then()
                                .statusCode(403);
        }

        @Test
        @Order(35)
        void testGetMyInvoiceByIdUnauthorized() {
                given()
                                .when()
                                .get("/api/v1/users/1/invoices/1")
                                .then()
                                .statusCode(401);
        }

        @Test
        @Order(36)
        void testGetMyInvoiceByIdNotFoundForVictor() throws JSONException {
                var victorCookies = loginAsUserVictor();
                Long victorId = getCurrentUserId(victorCookies);

                given()
                                .cookies(victorCookies)
                                .when()
                                .get("/api/v1/users/" + victorId + "/invoices/999999")
                                .then()
                                .statusCode(404);
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
        void testDownloadMyInvoicePdfForbiddenWhenVictorUsesGermanId() throws JSONException {
                var victorCookies = loginAsUserVictor();
                var germanCookies = loginAsAdminGerman();

                Long germanId = getCurrentUserId(germanCookies);

                given()
                                .cookies(victorCookies)
                                .when()
                                .get("/api/v1/users/" + germanId + "/invoices/1/download-pdf")
                                .then()
                                .statusCode(403);
        }

        @Test
        @Order(39)
        void testDownloadMyInvoicePdfNotFoundForVictor() throws JSONException {
                var victorCookies = loginAsUserVictor();
                Long victorId = getCurrentUserId(victorCookies);

                given()
                                .cookies(victorCookies)
                                .when()
                                .get("/api/v1/users/" + victorId + "/invoices/999999/download-pdf")
                                .then()
                                .statusCode(404);
        }

}
