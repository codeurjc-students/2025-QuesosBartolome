package es.codeurjc.quesosbartolome.system;

import io.restassured.RestAssured;
import io.restassured.http.Cookies;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "spring.profiles.active=test")
public class ApiReviewTests {

    @LocalServerPort
    int port;

    @BeforeEach
    void setup() {
        RestAssured.port = port;
        RestAssured.baseURI = "https://localhost";
        RestAssured.useRelaxedHTTPSValidation();
    }


    private Cookies login(String username, String password) throws JSONException {
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

    @Test
    void testGetReviewsByCheese_EmptyList() throws JSONException {
        var cookies = login("Tienda Artesanal de Riaza", "password123");

        given()
                .cookies(cookies)
                .when()
                .get("/api/v1/reviews/cheese/5?page=0&size=10")
                .then()
                .statusCode(200)
                .body("content", notNullValue());
    }

    @Test
    void testCreateReview_Unauthorized() throws JSONException {
        JSONObject body = new JSONObject();
        body.put("rating", 5);
        body.put("comment", "Muy bueno");
        body.put("cheeseId", 5);

        given()
                .contentType("application/json")
                .body(body.toString())
                .post("/api/v1/reviews")
                .then()
                .statusCode(401);
    }

    @Test
    void testCreateReview_BadRequest_InvalidRating() throws JSONException {
        var cookies = login("Tienda Artesanal de Riaza", "password123");

        JSONObject body = new JSONObject();
        body.put("rating", 10);
        body.put("comment", "mal");
        body.put("cheeseId", 5);

        given()
                .cookies(cookies)
                .contentType("application/json")
                .body(body.toString())
                .post("/api/v1/reviews")
                .then()
                .statusCode(400);
    }

    @Test
    void testCreateReview_Ok() throws JSONException {
        var cookies = login("Tienda Artesanal de Riaza", "password123");

        JSONObject body = new JSONObject();
        body.put("rating", 5);
        body.put("comment", "Excelente");
        body.put("cheeseId", 5);

        given()
                .cookies(cookies)
                .contentType("application/json")
                .body(body.toString())
                .post("/api/v1/reviews")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("rating", is(5))
                .body("comment", is("Excelente"));
    }

    @Test
    void testGetReviewById_NotFound() throws JSONException {
        var cookies = login("Tienda Artesanal de Riaza", "password123");

        given()
                .cookies(cookies)
                .when()
                .get("/api/v1/reviews/99999")
                .then()
                .statusCode(404);
    }

    @Test
    void testDeleteReview_Forbidden_WhenNotOwner() throws JSONException {
        var ownerCookies = login("Tienda Artesanal de Riaza", "password123");
        var otherCookies = login("Supermercado Aldeonte", "password123");

        JSONObject body = new JSONObject();
        body.put("rating", 4);
        body.put("comment", "Bien");
        body.put("cheeseId", 5);

        Number idNumber = given()
                .cookies(ownerCookies)
                .contentType("application/json")
                .body(body.toString())
                .post("/api/v1/reviews")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        Long reviewId = idNumber.longValue();

        given()
                .cookies(otherCookies)
                .when()
                .delete("/api/v1/reviews/" + reviewId)
                .then()
                .statusCode(403);
    }

    @Test
    void testDeleteReview_Ok() throws JSONException {
        var cookies = login("Tienda Artesanal de Riaza", "password123");

        JSONObject body = new JSONObject();
        body.put("rating", 5);
        body.put("comment", "Perfecto");
        body.put("cheeseId", 5);

        Number idNumber = given()
                .cookies(cookies)
                .contentType("application/json")
                .body(body.toString())
                .post("/api/v1/reviews")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        Long reviewId = idNumber.longValue();

        given()
                .cookies(cookies)
                .when()
                .delete("/api/v1/reviews/" + reviewId)
                .then()
                .statusCode(204);
    }

    @Test
    void testDeleteReview_NotFound() throws JSONException {
        var cookies = login("Tienda Artesanal de Riaza", "password123");

        given()
                .cookies(cookies)
                .when()
                .delete("/api/v1/reviews/99999")
                .then()
                .statusCode(404);
    }
}
