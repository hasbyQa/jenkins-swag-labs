package com.fakestoreapi.utils;

// Central place for all API constants — change once, applies everywhere
public final class ApiConfig {

    private ApiConfig() {}

    public static final String BASE_URL = "https://fakestoreapi.com";

    // Valid test credentials provided by the Fake Store API
    public static final String VALID_USERNAME = "johnd";
    public static final String VALID_PASSWORD = "m38rmF$";

    // Invalid credentials used in negative auth tests
    public static final String INVALID_USERNAME = "not_a_user";
    public static final String INVALID_PASSWORD  = "wrong_pass";
}
