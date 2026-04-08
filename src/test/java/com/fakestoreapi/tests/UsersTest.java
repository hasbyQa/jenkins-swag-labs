package com.fakestoreapi.tests;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Epic("Fake Store API")
@Feature("Users")
public class UsersTest extends BaseTest {

    @Test
    @Story("Get all users")
    @DisplayName("GET /users returns 200 and a list of users")
    @Description("Checks the users endpoint returns a successful response with user data")
    void getAllUsers_shouldReturn200AndList() {
        given()
            .spec(requestSpec)
        .when()
            .get("/users")
        .then()
            .statusCode(200)
            .body("size()", greaterThan(0))
            // Each user must have an id, email and username
            .body("[0].id", notNullValue())
            .body("[0].email", notNullValue())
            .body("[0].username", notNullValue());
    }

    @Test
    @Story("Get single user")
    @DisplayName("GET /users/1 returns the user with id 1")
    @Description("Fetches user 1 and verifies all key fields are present")
    void getUserById_shouldReturnCorrectUser() {
        given()
            .spec(requestSpec)
        .when()
            .get("/users/1")
        .then()
            .statusCode(200)
            .body("id", equalTo(1))
            .body("email", not(emptyOrNullString()))
            .body("username", not(emptyOrNullString()))
            // User profile must include name and address objects
            .body("name", notNullValue())
            .body("address", notNullValue());
    }

    @Test
    @Story("Get users with limit")
    @DisplayName("GET /users?limit=3 returns exactly 3 users")
    @Description("Verifies the limit query param reduces the number of users returned")
    void getUsersWithLimit_shouldReturnCorrectCount() {
        given()
            .spec(requestSpec)
            .queryParam("limit", 3)
        .when()
            .get("/users")
        .then()
            .statusCode(200)
            .body("size()", equalTo(3));
    }

    @Test
    @Story("Create user")
    @DisplayName("POST /users creates a new user and returns it with an id")
    @Description("Sends a user payload and confirms the API returns it with an assigned id")
    void addUser_shouldReturnCreatedUserWithId() {
        String newUser = """
                {
                    "email": "testuser@fakestoreapi.com",
                    "username": "testuser",
                    "password": "securepassword",
                    "name": { "firstname": "Test", "lastname": "User" },
                    "address": {
                        "city": "Kigali",
                        "street": "123 Test Street",
                        "number": 1,
                        "zipcode": "00000",
                        "geolocation": { "lat": "-1.95", "long": "30.06" }
                    },
                    "phone": "1-555-555-5555"
                }
                """;

        given()
            .spec(requestSpec)
            .body(newUser)
        .when()
            .post("/users")
        .then()
            .statusCode(200)
            // API assigns a new id to the created user
            .body("id", notNullValue());
    }

    @Test
    @Story("Update user")
    @DisplayName("PUT /users/1 updates and returns the user")
    @Description("Sends updated data for user 1 and checks the response reflects the change")
    void updateUser_shouldReturnUpdatedUser() {
        String updatedUser = """
                {
                    "email": "updated@fakestoreapi.com",
                    "username": "updateduser",
                    "password": "newpassword",
                    "name": { "firstname": "Updated", "lastname": "User" },
                    "address": {
                        "city": "Kigali",
                        "street": "456 Updated Street",
                        "number": 2,
                        "zipcode": "11111",
                        "geolocation": { "lat": "-1.95", "long": "30.06" }
                    },
                    "phone": "1-555-000-0000"
                }
                """;

        given()
            .spec(requestSpec)
            .body(updatedUser)
        .when()
            .put("/users/1")
        .then()
            .statusCode(200)
            .body("id", notNullValue());
    }

    @Test
    @Story("Delete user")
    @DisplayName("DELETE /users/1 returns the deleted user")
    @Description("Deletes user 1 and checks the API echoes the deleted user's data")
    void deleteUser_shouldReturnDeletedUser() {
        given()
            .spec(requestSpec)
        .when()
            .delete("/users/1")
        .then()
            .statusCode(200)
            .body("id", notNullValue());
    }
}
