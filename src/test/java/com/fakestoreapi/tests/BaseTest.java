package com.fakestoreapi.tests;

import com.fakestoreapi.utils.ApiConfig;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;

// Shared setup for all test classes — runs once before any test in a subclass
public abstract class BaseTest {

    // Reused across all tests to avoid repeating base URL and headers
    protected static RequestSpecification requestSpec;

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = ApiConfig.BASE_URL;

        requestSpec = new RequestSpecBuilder()
                .setBaseUri(ApiConfig.BASE_URL)
                .setContentType(ContentType.JSON)
                // Attaches every request and response to the Allure report automatically
                .addFilter(new AllureRestAssured())
                .build();
    }
}
