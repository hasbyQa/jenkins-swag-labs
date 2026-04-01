package com.swaglabs.tests;

import com.swaglabs.pages.InventoryPage;
import com.swaglabs.pages.LoginPage;
import com.swaglabs.utils.TestConfig;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Login feature.
 * Covers: successful login, failed login, locked-out user, empty fields.
 * 
 * 📊 DETAILED REPORTING:
 * - Each test includes step-by-step logging
 * - Screenshots captured at key moments
 * - Assertions logged with pass/fail details
 * - Timing information for performance tracking
 */
@DisplayName("🔐 Login Tests")
class LoginTest extends BaseTest {

    private LoginPage loginPage;

    @BeforeEach
    void openLoginPage() {
        logStep("Navigate to Swag Labs login page");
        loginPage = new LoginPage(driver).open();
        
        // Capture initial state
        captureScreenshot("Login page loaded");
        logPageLoaded("LoginPage", driver.getCurrentUrl());
        
        logger.info("📝 Login page ready for test execution");
    }

    // ═══════════════════════════════════════════════════════════
    // HAPPY PATH TESTS
    // ═══════════════════════════════════════════════════════════

    @Test
    @DisplayName("✅ Valid credentials redirect to Products page")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Verify that a user with valid credentials can successfully log in and land on the Products (Inventory) page")
    void validLogin_shouldLandOnInventoryPage() {
        logger.info("═══════════════════════════════════════════════════════════");
        logger.info("TEST: Valid Login Redirect to Inventory Page");
        logger.info("═══════════════════════════════════════════════════════════");
        
        logStep("Enter valid username: " + TestConfig.VALID_USER);
        logger.info("  Username: {}", TestConfig.VALID_USER);
        
        logStep("Enter valid password");
        logger.info("  Password: [REDACTED FOR SECURITY]");
        
        long loginStart = System.currentTimeMillis();
        InventoryPage inventoryPage = loginPage.loginAs(
                TestConfig.VALID_USER,
                TestConfig.VALID_PASSWORD
        );
        long loginTime = System.currentTimeMillis() - loginStart;
        logger.info("⏱️  Login completed in {}ms", loginTime);

        captureScreenshot("After successful login - Inventory page");
        
        logStep("Verify Inventory page is loaded");
        boolean isLoaded = inventoryPage.isLoaded();
        logAssertion("Inventory page is loaded", isLoaded);
        assertTrue(isLoaded, "Inventory page should be visible after successful login");

        logStep("Verify page title is 'Products'");
        String pageTitle = inventoryPage.getPageTitle();
        logger.info("  Actual page title: '{}'", pageTitle);
        logger.info("  Expected page title: '{}'", TestConfig.PRODUCTS_TITLE);
        boolean titleMatch = TestConfig.PRODUCTS_TITLE.equals(pageTitle);
        logAssertion("Page title matches expected value", titleMatch);
        assertEquals(TestConfig.PRODUCTS_TITLE, pageTitle, "Page title should read 'Products'");
        
        logger.info("═══════════════════════════════════════════════════════════");
        logger.info("✅ TEST PASSED: Valid login successful");
        logger.info("═══════════════════════════════════════════════════════════");
    }

    @Test
    @DisplayName("✅ URL changes to /inventory.html after login")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify that the URL updates to contain 'inventory' after successful login")
    void validLogin_urlShouldContainInventory() {
        logger.info("═══════════════════════════════════════════════════════════");
        logger.info("TEST: URL Verification After Login");
        logger.info("═══════════════════════════════════════════════════════════");
        
        logStep("Perform login with valid credentials");
        loginPage.loginAs(TestConfig.VALID_USER, TestConfig.VALID_PASSWORD);
        
        String currentUrl = driver.getCurrentUrl();
        logger.info("  Current URL: {}", currentUrl);
        
        logStep("Verify URL contains 'inventory'");
        boolean urlContainsInventory = currentUrl.contains("inventory");
        logAssertion("URL contains 'inventory'", urlContainsInventory);
        
        captureScreenshot("URL verified after login");
        
        assertTrue(urlContainsInventory, "URL should contain 'inventory' after login");
        
        logger.info("═══════════════════════════════════════════════════════════");
        logger.info("✅ TEST PASSED: URL verification successful");
        logger.info("═══════════════════════════════════════════════════════════");
    }

    // ═══════════════════════════════════════════════════════════
    // NEGATIVE TESTS - INVALID CREDENTIALS
    // ═══════════════════════════════════════════════════════════

    @Test
    @DisplayName("❌ Wrong password shows error banner")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify that entering incorrect password displays appropriate error message")
    void invalidPassword_shouldShowError() {
        logger.info("═══════════════════════════════════════════════════════════");
        logger.info("TEST: Invalid Password Error Handling");
        logger.info("═══════════════════════════════════════════════════════════");
        
        logStep("Enter valid username with INVALID password");
        logger.info("  Username: {}", TestConfig.VALID_USER);
        logger.info("  Password: [INVALID - REDACTED]");
        
        loginPage.attemptLogin(TestConfig.VALID_USER, TestConfig.INVALID_PASSWORD);
        
        captureScreenshot("Error message displayed for wrong password");

        logStep("Verify error message is displayed");
        boolean errorDisplayed = loginPage.isErrorDisplayed();
        logAssertion("Error message displayed", errorDisplayed);
        assertTrue(errorDisplayed, "Error message should appear for wrong password");

        logStep("Verify error message contains 'Epic sadface'");
        String errorMessage = loginPage.getErrorMessage();
        logger.info("  Actual error message: '{}'", errorMessage);
        logger.info("  Expected to contain: '{}'", TestConfig.LOGIN_ERROR_MESSAGE);
        boolean messageCorrect = errorMessage.contains(TestConfig.LOGIN_ERROR_MESSAGE);
        logAssertion("Error message is correct", messageCorrect);
        assertTrue(messageCorrect, "Error text should start with 'Epic sadface:'");
        
        logger.info("═══════════════════════════════════════════════════════════");
        logger.info("✅ TEST PASSED: Invalid password error handling works");
        logger.info("═══════════════════════════════════════════════════════════");
    }

    @Test
    @DisplayName("❌ Wrong username shows error banner")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify that entering incorrect username displays appropriate error message")
    void invalidUsername_shouldShowError() {
        logger.info("═══════════════════════════════════════════════════════════");
        logger.info("TEST: Invalid Username Error Handling");
        logger.info("═══════════════════════════════════════════════════════════");
        
        logStep("Enter INVALID username with valid password");
        logger.info("  Username: {} [INVALID]", TestConfig.INVALID_USER);
        logger.info("  Password: [VALID - REDACTED]");
        
        loginPage.attemptLogin(TestConfig.INVALID_USER, TestConfig.VALID_PASSWORD);
        
        captureScreenshot("Error message displayed for wrong username");

        logStep("Verify error message is displayed");
        boolean errorDisplayed = loginPage.isErrorDisplayed();
        logAssertion("Error message displayed", errorDisplayed);
        assertTrue(errorDisplayed, "Error message should appear for wrong username");
        
        String errorMessage = loginPage.getErrorMessage();
        logger.info("  Error message: '{}'", errorMessage);
        
        logger.info("═══════════════════════════════════════════════════════════");
        logger.info("✅ TEST PASSED: Invalid username error handling works");
        logger.info("═══════════════════════════════════════════════════════════");
    }

    // ═══════════════════════════════════════════════════════════
    // NEGATIVE TESTS - ACCOUNT LOCKED
    // ═══════════════════════════════════════════════════════════

    @Test
    @DisplayName("❌ Locked-out user cannot log in")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify that a locked-out user receives appropriate error message")
    void lockedOutUser_shouldSeeError() {
        logger.info("═══════════════════════════════════════════════════════════");
        logger.info("TEST: Locked User Account Verification");
        logger.info("═══════════════════════════════════════════════════════════");
        
        logStep("Attempt to login with LOCKED user account");
        logger.info("  Username: {} [LOCKED]", TestConfig.LOCKED_USER);
        logger.info("  Password: [VALID - REDACTED]");
        
        loginPage.attemptLogin(TestConfig.LOCKED_USER, TestConfig.VALID_PASSWORD);
        
        captureScreenshot("Error message for locked account");

        logStep("Verify error is displayed");
        boolean errorDisplayed = loginPage.isErrorDisplayed();
        logAssertion("Error displayed for locked account", errorDisplayed);
        assertTrue(errorDisplayed, "Locked-out user should see an error");

        logStep("Verify error message mentions 'locked'");
        String errorMessage = loginPage.getErrorMessage();
        logger.info("  Error message: '{}'", errorMessage);
        boolean mentionsLocked = errorMessage.toLowerCase().contains("locked");
        logAssertion("Error mentions 'locked'", mentionsLocked);
        assertTrue(mentionsLocked, "Error should mention 'locked'");
        
        logger.info("═══════════════════════════════════════════════════════════");
        logger.info("✅ TEST PASSED: Locked user error handling works");
        logger.info("═══════════════════════════════════════════════════════════");
    }

    // ═══════════════════════════════════════════════════════════
    // NEGATIVE TESTS - EMPTY FIELDS
    // ═══════════════════════════════════════════════════════════

    @Test
    @DisplayName("❌ Empty credentials shows error banner")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify that submitting empty credentials displays error message")
    void emptyCredentials_shouldShowError() {
        logger.info("═══════════════════════════════════════════════════════════");
        logger.info("TEST: Empty Credentials Validation");
        logger.info("═══════════════════════════════════════════════════════════");
        
        logStep("Submit empty username and password");
        logger.info("  Username: [EMPTY]");
        logger.info("  Password: [EMPTY]");
        
        loginPage.attemptLogin("", "");
        
        captureScreenshot("Error message for empty fields");

        logStep("Verify error message is displayed");
        boolean errorDisplayed = loginPage.isErrorDisplayed();
        logAssertion("Error displayed for empty fields", errorDisplayed);
        assertTrue(errorDisplayed, "Submitting empty form should show an error");
        
        String errorMessage = loginPage.getErrorMessage();
        logger.info("  Error message: '{}'", errorMessage);
        
        logger.info("═══════════════════════════════════════════════════════════");
        logger.info("✅ TEST PASSED: Empty field validation works");
        logger.info("═══════════════════════════════════════════════════════════");
    }
}