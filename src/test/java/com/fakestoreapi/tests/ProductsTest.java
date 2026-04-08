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
@Feature("Products")
public class ProductsTest extends BaseTest {

    @Test
    @Story("Get all products")
    @DisplayName("GET /products returns 200 and a list of products")
    @Description("Checks the products endpoint returns a successful response with items")
    void getAllProducts_shouldReturn200AndNonEmptyList() {
        given()
            .spec(requestSpec)
        .when()
            .get("/products")
        .then()
            .statusCode(200)
            // API returns 20 products by default
            .body("size()", greaterThan(0))
            .body("[0].id", notNullValue())
            .body("[0].title", notNullValue())
            .body("[0].price", notNullValue());
    }

    @Test
    @Story("Get single product")
    @DisplayName("GET /products/1 returns the correct product")
    @Description("Fetches product with id=1 and verifies the returned id matches")
    void getProductById_shouldReturnCorrectProduct() {
        given()
            .spec(requestSpec)
        .when()
            .get("/products/1")
        .then()
            .statusCode(200)
            .body("id", equalTo(1))
            .body("title", not(emptyOrNullString()))
            .body("price", greaterThan(0.0f))
            .body("category", not(emptyOrNullString()));
    }

    @Test
    @Story("Limit results")
    @DisplayName("GET /products?limit=5 returns exactly 5 products")
    @Description("The limit query param should cap the number of returned products")
    void getProductsWithLimit_shouldReturnCorrectCount() {
        given()
            .spec(requestSpec)
            .queryParam("limit", 5)
        .when()
            .get("/products")
        .then()
            .statusCode(200)
            .body("size()", equalTo(5));
    }

    @Test
    @Story("Sort products")
    @DisplayName("GET /products?sort=desc returns products in descending order")
    @Description("Checks that sorting by desc returns a valid list")
    void getProductsSortedDesc_shouldReturn200() {
        given()
            .spec(requestSpec)
            .queryParam("sort", "desc")
        .when()
            .get("/products")
        .then()
            .statusCode(200)
            .body("size()", greaterThan(0));
    }

    @Test
    @Story("Get categories")
    @DisplayName("GET /products/categories returns all categories")
    @Description("Checks the categories endpoint returns the expected category names")
    void getAllCategories_shouldReturnExpectedCategories() {
        given()
            .spec(requestSpec)
        .when()
            .get("/products/categories")
        .then()
            .statusCode(200)
            .body("size()", equalTo(4))
            // Verify the four known Fake Store categories are present
            .body("$", hasItems("electronics", "jewelery", "men's clothing", "women's clothing"));
    }

    @Test
    @Story("Filter by category")
    @DisplayName("GET /products/category/electronics returns only electronics")
    @Description("Every product returned must belong to the electronics category")
    void getProductsByCategory_shouldReturnOnlyThatCategory() {
        given()
            .spec(requestSpec)
        .when()
            .get("/products/category/electronics")
        .then()
            .statusCode(200)
            .body("size()", greaterThan(0))
            // All returned items must be electronics — not a mix
            .body("category", everyItem(equalTo("electronics")));
    }

    @Test
    @Story("Create product")
    @DisplayName("POST /products creates a new product and returns it with an id")
    @Description("Sends a new product payload and checks the API echoes it back with an assigned id")
    void addProduct_shouldReturnCreatedProductWithId() {
        String newProduct = """
                {
                    "title": "Test Product",
                    "price": 29.99,
                    "description": "A product added by the test suite",
                    "image": "https://fakestoreapi.com/img/81fAn1uwcFL._AC_UY879_.jpg",
                    "category": "electronics"
                }
                """;

        given()
            .spec(requestSpec)
            .body(newProduct)
        .when()
            .post("/products")
        .then()
            .statusCode(201)
            // API assigns a new id to every created product
            .body("id", notNullValue())
            .body("title", equalTo("Test Product"))
            .body("price", equalTo(29.99f));
    }

    @Test
    @Story("Update product")
    @DisplayName("PUT /products/7 updates and returns the product")
    @Description("Sends updated data for product 7 and verifies the response reflects the changes")
    void updateProduct_shouldReturnUpdatedData() {
        String updatedProduct = """
                {
                    "title": "Updated Product Title",
                    "price": 49.99,
                    "description": "Updated by the test suite",
                    "image": "https://fakestoreapi.com/img/81fAn1uwcFL._AC_UY879_.jpg",
                    "category": "men's clothing"
                }
                """;

        given()
            .spec(requestSpec)
            .body(updatedProduct)
        .when()
            .put("/products/7")
        .then()
            .statusCode(200)
            .body("id", notNullValue())
            .body("title", equalTo("Updated Product Title"))
            .body("price", equalTo(49.99f));
    }

    @Test
    @Story("Delete product")
    @DisplayName("DELETE /products/6 returns the deleted product")
    @Description("Deletes product 6 and checks the API returns the deleted item's data")
    void deleteProduct_shouldReturnDeletedProduct() {
        given()
            .spec(requestSpec)
        .when()
            .delete("/products/6")
        .then()
            .statusCode(200)
            // API echoes back the deleted product rather than an empty body
            .body("id", notNullValue());
    }
}
