package es.codeurjc.quesosbartolome.system;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "spring.profiles.active=test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ApiCheeseTests {

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

    /**
     * Create an auxiliary cheese temporarily for edit/delete tests
     * 
     * @param cookies Admin authentication cookies
     * @param name    Name of the auxiliary cheese
     * @return ID of the created cheese
     */
    private Long createAuxiliaryCheese(io.restassured.http.Cookies cookies, String name) throws JSONException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("id", JSONObject.NULL);
        requestBody.put("name", name);
        requestBody.put("price", 10.0);
        requestBody.put("description", "Queso auxiliar para test");
        requestBody.put("manufactureDate", "2024-01-01");
        requestBody.put("expirationDate", "2025-01-01");
        requestBody.put("type", "Pasta Prensada");
        requestBody.put("boxes", new JSONArray(List.of(1.0, 2.0)));

        return given()
                .cookies(cookies)
                .contentType("application/json")
                .body(requestBody.toString())
                .when()
                .post("/api/v1/cheeses/new")
                .then()
                .statusCode(201)
                .extract()
                .jsonPath()
                .getLong("id");
    }

    @Test
    @Order(1)
    void testGetAllCheeses() {
        when()
                .get("/api/v1/cheeses")
                .then()
                .statusCode(200)
                .body("$", not(empty()))
                .body("[0].name", equalTo("Semicurado"))
                .body("[1].name", equalTo("Azul"));
    }

    @Test
    @Order(2)
    void testGetCheeseById() {
        when()
                .get("/api/v1/cheeses/1")
                .then()
                .statusCode(200)
                .body("id", equalTo(1))
                .body("name", equalTo("Semicurado"));
    }

    @Test
    @Order(3)
    void testGetCheeseById_NotFound() {
        when()
                .get("/api/v1/cheeses/999")
                .then()
                .statusCode(404);
    }

    @Test
    @Order(4)
    void testGetCheeseImage_Ok() {
        when()
                .get("/api/v1/cheeses/1/image")
                .then()
                .statusCode(200)
                .header("Content-Type", equalTo("image/png"))
                .body(not(empty()));
    }

    @Test
    @Order(5)
    void testGetCheeseImage_NotFound() {
        when()
                .get("/api/v1/cheeses/999/image")
                .then()
                .statusCode(404);
    }

    @Test
    @Order(6)
    void testCreateCheese_Unauthorized() {
        given()
                .contentType("application/json")
                .body("{\"name\":\"NuevoQueso\"}")
                .when()
                .post("/api/v1/cheeses/new")
                .then()
                .statusCode(401);
    }

    @Test
    @Order(7)
    void testCreateCheese_Forbidden() throws JSONException {
        var cookies = login("Victor", "password123");

        given()
                .cookies(cookies)
                .contentType("application/json")
                .body("{\"name\":\"NuevoQueso\"}")
                .when()
                .post("/api/v1/cheeses/new")
                .then()
                .statusCode(403);
    }

    @Test
    @Order(8)
    void testCreateCheese_BadRequest() throws JSONException {
        var cookies = login("German", "password123");

        given()
                .cookies(cookies)
                .contentType("application/json")
                .body("{}")
                .when()
                .post("/api/v1/cheeses/new")
                .then()
                .statusCode(400);
    }

    @Test
    @Order(9)
    void testCreateCheese_Created() throws JSONException {
        var cookies = login("German", "password123");

        JSONObject requestBody = new JSONObject();
        requestBody.put("id", JSONObject.NULL);
        requestBody.put("name", "NuevoQueso");
        requestBody.put("price", 12.5);
        requestBody.put("description", "desc");
        requestBody.put("manufactureDate", "2024-01-01");
        requestBody.put("expirationDate", "2025-01-01");
        requestBody.put("type", "Pasta Prensada");
        requestBody.put("boxes", new JSONArray(List.of(1.0, 2.0)));

        given()
                .cookies(cookies)
                .contentType("application/json")
                .body(requestBody.toString())
                .when()
                .post("/api/v1/cheeses/new")
                .then()
                .statusCode(201)
                .header("Location", containsString("/api/v1/cheeses/"))
                .body("name", equalTo("NuevoQueso"));
    }

    @Test
    @Order(10)
    void testUpdateCheese_Unauthorized() {
        given()
                .contentType("application/json")
                .body("{\"name\":\"Nuevo\"}")
                .when()
                .put("/api/v1/cheeses/1")
                .then()
                .statusCode(401);
    }

    @Test
    @Order(11)
    void testUpdateCheese_Forbidden() throws JSONException {
        var cookies = login("Victor", "password123");

        given()
                .cookies(cookies)
                .contentType("application/json")
                .body("{\"name\":\"Nuevo\"}")
                .when()
                .put("/api/v1/cheeses/1")
                .then()
                .statusCode(403);
    }

    @Test
    @Order(12)
    void testUpdateCheese_BadRequest() throws JSONException {
        var cookies = login("German", "password123");

        given()
                .cookies(cookies)
                .contentType("application/json")
                .body("{}")
                .when()
                .put("/api/v1/cheeses/1")
                .then()
                .statusCode(400);
    }

    @Test
    @Order(13)
    void testUpdateCheese_Ok() throws JSONException {
        var cookies = login("German", "password123");

        Long cheeseId = createAuxiliaryCheese(cookies, "QuesoParaActualizar");

        JSONObject body = new JSONObject();
        body.put("id", cheeseId);
        body.put("name", "QuesoActualizado");
        body.put("price", 15.0);
        body.put("description", "desc actualizada");
        body.put("manufactureDate", "2024-01-01");
        body.put("expirationDate", "2025-01-01");
        body.put("Type", "Curado");
        body.put("boxes", new JSONArray(List.of(1.0)));

        given()
                .cookies(cookies)
                .contentType("application/json")
                .body(body.toString())
                .when()
                .put("/api/v1/cheeses/" + cheeseId)
                .then()
                .statusCode(200)
                .body("name", equalTo("QuesoActualizado"));
    }

    @Test
    @Order(14)
    void testUpdateCheeseImage_Unauthorized() {
        given()
                .multiPart("file", "imagen.png", "fake".getBytes())
                .when()
                .put("/api/v1/cheeses/1/image")
                .then()
                .statusCode(401);
    }

    @Test
    @Order(15)
    void testUpdateCheeseImage_Forbidden() throws JSONException {
        var cookies = login("Victor", "password123");

        given()
                .cookies(cookies)
                .multiPart("file", "imagen.png", "fake".getBytes())
                .when()
                .put("/api/v1/cheeses/1/image")
                .then()
                .statusCode(403);
    }

    @Test
    @Order(16)
    void testUpdateCheeseImage_NotFound() throws JSONException {
        var cookies = login("German", "password123");

        given()
                .cookies(cookies)
                .multiPart("file", "imagen.png", "fake".getBytes())
                .when()
                .put("/api/v1/cheeses/999/image")
                .then()
                .statusCode(404);
    }

    @Test
    @Order(17)
    void testUpdateCheeseImage_Ok() throws JSONException {
        var cookies = login("German", "password123");

        Long cheeseId = createAuxiliaryCheese(cookies, "QuesoParaImagenUpdate");

        given()
                .cookies(cookies)
                .multiPart("file", "imagen.png", "fake".getBytes())
                .when()
                .put("/api/v1/cheeses/" + cheeseId + "/image")
                .then()
                .statusCode(200);
    }

    @Test
    @Order(18)
    void testUploadCheeseImage_Unauthorized() {
        given()
                .multiPart("file", "imagen.png", "fakecontent".getBytes())
                .when()
                .post("/api/v1/cheeses/1/image")
                .then()
                .statusCode(401);
    }

    @Test
    @Order(19)
    void testUploadCheeseImage_Forbidden() throws JSONException {
        var cookies = login("Victor", "password123");

        given()
                .cookies(cookies)
                .multiPart("file", "imagen.png", "fakecontent".getBytes())
                .when()
                .post("/api/v1/cheeses/1/image")
                .then()
                .statusCode(403);
    }

    @Test
    @Order(20)
    void testUploadCheeseImage_NotFound() throws JSONException {
        var cookies = login("German", "password123");

        given()
                .cookies(cookies)
                .multiPart("file", "imagen.png", "fakecontent".getBytes())
                .when()
                .post("/api/v1/cheeses/999/image")
                .then()
                .statusCode(404);
    }

    @Test
    @Order(21)
    void testUploadCheeseImage_Ok() throws JSONException {
        var cookies = login("German", "password123");

        Long cheeseId = createAuxiliaryCheese(cookies, "QuesoParaImagenUpload");

        given()
                .cookies(cookies)
                .multiPart("file", "imagen.png", "fakecontent".getBytes())
                .when()
                .post("/api/v1/cheeses/" + cheeseId + "/image")
                .then()
                .statusCode(200);
    }

    @Test
    @Order(22)
    void testDeleteCheese_Unauthorized() {
        when()
                .delete("/api/v1/cheeses/1")
                .then()
                .statusCode(401);
    }

    @Test
    @Order(23)
    void testDeleteCheese_Forbidden() throws JSONException {
        var cookies = login("Victor", "password123");

        given()
                .cookies(cookies)
                .when()
                .delete("/api/v1/cheeses/1")
                .then()
                .statusCode(403);
    }

    @Test
    @Order(24)
    void testDeleteCheese_NotFound() throws JSONException {
        var cookies = login("German", "password123");

        given()
                .cookies(cookies)
                .when()
                .delete("/api/v1/cheeses/999")
                .then()
                .statusCode(404);
    }

    @Test
    @Order(25)
    void testDeleteCheese_NoContent() throws JSONException {
        var cookies = login("German", "password123");

        Long cheeseId = createAuxiliaryCheese(cookies, "QuesoParaEliminar");

        given()
                .cookies(cookies)
                .when()
                .delete("/api/v1/cheeses/" + cheeseId)
                .then()
                .statusCode(204);
    }

    @Test
    @Order(26)
    void testAddBox_Unauthorized() {
        given()
                .contentType("application/json")
                .body("{\"weight\": 2.5}")
                .when()
                .put("/api/v1/cheeses/1/boxes/add")
                .then()
                .statusCode(401);
    }

    @Test
    @Order(27)
    void testAddBox_Forbidden() throws Exception {
        var cookies = login("Victor", "password123");

        given()
                .cookies(cookies)
                .contentType("application/json")
                .body("{\"weight\": 2.5}")
                .when()
                .put("/api/v1/cheeses/1/boxes/add")
                .then()
                .statusCode(403);
    }

    @Test
    @Order(28)
    void testAddBox_BadRequest() throws Exception {
        var cookies = login("German", "password123");

        given()
                .cookies(cookies)
                .contentType("application/json")
                .body("{\"weight\": -1}")
                .when()
                .put("/api/v1/cheeses/1/boxes/add")
                .then()
                .statusCode(400);
    }

    @Test
    @Order(29)
    void testAddBox_NotFound() throws Exception {
        var cookies = login("German", "password123");

        given()
                .cookies(cookies)
                .contentType("application/json")
                .body("{\"weight\": 2.5}")
                .when()
                .put("/api/v1/cheeses/999/boxes/add")
                .then()
                .statusCode(404);
    }

    @Test
    @Order(30)
    void testAddBox_Ok() throws Exception {
        var cookies = login("German", "password123");

        Long cheeseId = createAuxiliaryCheese(cookies, "QuesoAddBox");

        JSONObject body = new JSONObject();
        body.put("weight", 3.0);

        given()
                .cookies(cookies)
                .contentType("application/json")
                .body(body.toString())
                .when()
                .put("/api/v1/cheeses/" + cheeseId + "/boxes/add")
                .then()
                .statusCode(200)
                .body("boxes", hasItem(3.0f));
    }

    @Test
    @Order(31)
    void testRemoveBox_Unauthorized() {
        when()
                .put("/api/v1/cheeses/1/boxes/remove/0")
                .then()
                .statusCode(401);
    }

    @Test
    @Order(32)
    void testRemoveBox_Forbidden() throws Exception {
        var cookies = login("Victor", "password123");

        given()
                .cookies(cookies)
                .when()
                .put("/api/v1/cheeses/1/boxes/remove/0")
                .then()
                .statusCode(403);
    }

    @Test
    @Order(33)
    void testRemoveBox_BadRequest() throws Exception {
        var cookies = login("German", "password123");

        Long cheeseId = createAuxiliaryCheese(cookies, "QuesoRemoveBad");

        given()
                .cookies(cookies)
                .when()
                .put("/api/v1/cheeses/" + cheeseId + "/boxes/remove/5")
                .then()
                .statusCode(400);
    }

    @Test
    @Order(34)
    void testRemoveBox_NotFound() throws Exception {
        var cookies = login("German", "password123");

        given()
                .cookies(cookies)
                .when()
                .put("/api/v1/cheeses/999/boxes/remove/0")
                .then()
                .statusCode(404);
    }

    @Test
    @Order(35)
    void testRemoveBox_Ok() throws Exception {
        var cookies = login("German", "password123");

        Long cheeseId = createAuxiliaryCheese(cookies, "QuesoRemoveOk");

        given()
                .cookies(cookies)
                .when()
                .put("/api/v1/cheeses/" + cheeseId + "/boxes/remove/0")
                .then()
                .statusCode(200)
                .body("boxes.size()", equalTo(1));
    }

}
