package com.fakestoreapi.tests;

import com.fakestoreapi.utils.ApiConfig;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Epic("Fake Store API")
@Feature("Authentication")
public class AuthTest extends BaseTest {

    @Test
    @Story("Login")
    @DisplayName("POST /auth/login with valid credentials returns a token")
    @Description("Checks that valid credentials produce a JWT token in the response")
    void login_withValidCredentials_shouldReturnToken() {
        String credentials = String.format("""
                {
                    "username": "%s",
                    "password": "%s"
                }
                """, ApiConfig.VALID_USERNAME, ApiConfig.VALID_PASSWORD);

        given()
            .spec(requestSpec)
            .body(credentials)
        .when()
            .post("/auth/login")
        .then()
            .statusCode(201)
            // A non-empty token confirms successful authentication
            .body("token", not(emptyOrNullString()));
    }

    @Test
    @Story("Login")
    @DisplayName("POST /auth/login with invalid credentials returns 401")
    @Description("Checks that wrong credentials are rejected with HTTP 401")
    void login_withInvalidCredentials_shouldNotReturnToken() {
        String badCredentials = String.format("""
                {
                    "username": "%s",
                    "password": "%s"
                }
                """, ApiConfig.INVALID_USERNAME, ApiConfig.INVALID_PASSWORD);

        given()
            .spec(requestSpec)
            .body(badCredentials)
        .when()
            .post("/auth/login")
        .then()
            // API returns 401 with a plain-text error message for unrecognised credentials
            .statusCode(401);
    }
}
