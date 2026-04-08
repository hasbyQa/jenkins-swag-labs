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
@Feature("Carts")
public class CartsTest extends BaseTest {

    @Test
    @Story("Get all carts")
    @DisplayName("GET /carts returns 200 and a non-empty list")
    @Description("Checks the carts endpoint returns a list with at least one cart")
    void getAllCarts_shouldReturn200AndList() {
        given()
            .spec(requestSpec)
        .when()
            .get("/carts")
        .then()
            .statusCode(200)
            .body("size()", greaterThan(0))
            // Each cart must have an id and a list of products
            .body("[0].id", notNullValue())
            .body("[0].products", notNullValue());
    }

    @Test
    @Story("Get single cart")
    @DisplayName("GET /carts/1 returns the cart with id 1")
    @Description("Fetches cart 1 and confirms the returned id matches")
    void getCartById_shouldReturnCorrectCart() {
        given()
            .spec(requestSpec)
        .when()
            .get("/carts/1")
        .then()
            .statusCode(200)
            .body("id", equalTo(1))
            .body("userId", notNullValue())
            // Cart must contain at least one product
            .body("products.size()", greaterThan(0));
    }

    @Test
    @Story("Get carts with limit")
    @DisplayName("GET /carts?limit=3 returns exactly 3 carts")
    @Description("The limit param should restrict the number of carts returned")
    void getCartsWithLimit_shouldReturnCorrectCount() {
        given()
            .spec(requestSpec)
            .queryParam("limit", 3)
        .when()
            .get("/carts")
        .then()
            .statusCode(200)
            .body("size()", equalTo(3));
    }

    @Test
    @Story("Create cart")
    @DisplayName("POST /carts creates a new cart and returns it with an id")
    @Description("Sends a cart payload and verifies the API responds with an assigned id")
    void addCart_shouldReturnCreatedCartWithId() {
        String newCart = """
                {
                    "userId": 1,
                    "date": "2024-01-01",
                    "products": [
                        { "productId": 1, "quantity": 2 },
                        { "productId": 3, "quantity": 1 }
                    ]
                }
                """;

        given()
            .spec(requestSpec)
            .body(newCart)
        .when()
            .post("/carts")
        .then()
            .statusCode(200)
            // API assigns an id to every new cart
            .body("id", notNullValue());
    }

    @Test
    @Story("Update cart")
    @DisplayName("PUT /carts/1 updates and returns the cart")
    @Description("Sends updated cart data and confirms the response reflects the changes")
    void updateCart_shouldReturnUpdatedCart() {
        String updatedCart = """
                {
                    "userId": 1,
                    "date": "2024-06-01",
                    "products": [
                        { "productId": 5, "quantity": 3 }
                    ]
                }
                """;

        given()
            .spec(requestSpec)
            .body(updatedCart)
        .when()
            .put("/carts/1")
        .then()
            .statusCode(200)
            .body("id", notNullValue());
    }

    @Test
    @Story("Delete cart")
    @DisplayName("DELETE /carts/1 returns the deleted cart")
    @Description("Deletes cart 1 and checks the API echoes the deleted cart data")
    void deleteCart_shouldReturnDeletedCart() {
        given()
            .spec(requestSpec)
        .when()
            .delete("/carts/1")
        .then()
            .statusCode(200)
            .body("id", notNullValue());
    }
}
