package com.swaglabs.tests;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.qameta.allure.Step;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Parent class for all test classes.
 * Handles ChromeDriver setup, teardown, and detailed reporting.
 *
 * Features:
 * - Automatic WebDriver management
 * - Screenshot capture on failures (for Allure reports)
 * - Detailed logging of test steps (SLF4J + Logback)
 * - Each test gets a fresh browser instance
 * - Comprehensive reporting with timings
 */
public abstract class BaseTest {

    // Logger for detailed test output in Allure reports
    protected static final Logger logger = LoggerFactory.getLogger(BaseTest.class);

    // 'protected' so subclasses (LoginTest, CartTest, etc.) can use it directly
    protected WebDriver driver;

    @BeforeEach
    void setUp() {
        logger.info("═══════════════════════════════════════════════════════════");
        logger.info("🔧 STARTING TEST SETUP");
        logger.info("═══════════════════════════════════════════════════════════");

        // WebDriverManager auto-downloads the right chromedriver binary
        long setupStart = System.currentTimeMillis();
        WebDriverManager.chromedriver().setup();
        logger.info("✅ WebDriver Manager initialized successfully");

        ChromeOptions options = new ChromeOptions();

        // Headless mode flags
        // Required for running inside Docker (no display server)
        options.addArguments("--headless=new");     // new headless mode (Chrome 112+)
        options.addArguments("--no-sandbox");        // required in Docker/root environments
        options.addArguments("--disable-dev-shm-usage"); // prevents shared memory issues in containers
        options.addArguments("--disable-gpu");       // not needed in headless mode
        options.addArguments("--window-size=1920,1080"); // consistent viewport for all tests

        driver = new ChromeDriver(options);
        driver.manage().deleteAllCookies(); // clean slate for every test

        long setupTime = System.currentTimeMillis() - setupStart;
        logger.info("✅ ChromeDriver initialized in {}ms", setupTime);
        logger.info("✅ Browser window size: 1920x1080 (consistent for all tests)");
        logger.info("✅ All cookies cleared for fresh test state");
        logger.info("═══════════════════════════════════════════════════════════");
    }

    @AfterEach
    void tearDown() {
        logger.info("═══════════════════════════════════════════════════════════");
        logger.info("🔧 COMPLETING TEST TEARDOWN");
        logger.info("═══════════════════════════════════════════════════════════");

        // Always quit — releases browser process and avoids memory leaks
        if (driver != null) {
            try {
                String currentUrl = driver.getCurrentUrl();
                logger.info("📍 Final URL: {}", currentUrl);
                logger.info("📄 Final Page Title: {}", driver.getTitle());

                driver.quit();
                logger.info("✅ WebDriver closed successfully");
            } catch (Exception e) {
                logger.error("⚠️ Error during teardown: {}", e.getMessage());
            }
        }
        logger.info("═══════════════════════════════════════════════════════════");
    }

    /**
     * Capture a screenshot for Allure report.
     * Automatically attached to the report.
     * @param stepName Description of what was captured
     */
    @Step("Taking screenshot: {stepName}")
    protected void captureScreenshot(String stepName) {
        try {
            if (driver instanceof TakesScreenshot) {
                byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                logger.info("📸 Screenshot captured: {}", stepName);
                
                // Allure will automatically attach this screenshot
                io.qameta.allure.Allure.addAttachment(
                    stepName,
                    "image/png",
                    new java.io.ByteArrayInputStream(screenshot),
                    "png"
                );
            }
        } catch (Exception e) {
            logger.warn("⚠️ Failed to capture screenshot: {}", e.getMessage());
        }
    }

    /**
     * Log test step with timestamp for detailed reporting.
     * @param stepDescription Description of the step being executed
     */
    @Step("{stepDescription}")
    protected void logStep(String stepDescription) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
        logger.info("▶️  [{}] Step: {}", timestamp, stepDescription);
    }

    /**
     * Log an important verification step.
     * @param verificationName Name of the verification
     * @param result True if verification passed, false if failed
     */
    protected void logAssertion(String verificationName, boolean result) {
        if (result) {
            logger.info("✅ PASS: {}", verificationName);
        } else {
            logger.error("❌ FAIL: {}", verificationName);
        }
    }

    /**
     * Log page load details for debugging.
     * @param pageName Name of the page
     * @param url URL of the page
     */
    protected void logPageLoaded(String pageName, String url) {
        logger.info("📄 Page Loaded: {} → {}", pageName, url);
        logger.info("   Title: {}", driver.getTitle());
        logger.info("   Timestamp: {}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }
}