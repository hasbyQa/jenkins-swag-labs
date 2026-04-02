package com.swaglabs.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

// Covers all 3 checkout steps:
//   Step 1 - fill info  (/checkout-step-one.html)
//   Step 2 - overview   (/checkout-step-two.html)
//   Done   - confirmed  (/checkout-complete.html)
public class CheckoutPage extends BasePage {

    // Confirmed from live DOM: camelCase data-test values
    private static final By FIRST_NAME_INPUT = By.cssSelector("[data-test='firstName']");
    private static final By LAST_NAME_INPUT  = By.cssSelector("[data-test='lastName']");
    private static final By POSTAL_CODE      = By.cssSelector("[data-test='postalCode']");
    private static final By CONTINUE_BUTTON  = By.cssSelector("[data-test='continue']");
    private static final By ERROR_MESSAGE    = By.cssSelector("[data-test='error']");

    // Step 2
    private static final By FINISH_BUTTON = By.cssSelector("[data-test='finish']");
    private static final By ITEM_TOTAL    = By.cssSelector("[data-test='subtotal-label']");

    // Confirmation
    private static final By COMPLETE_HEADER  = By.cssSelector("[data-test='complete-header']");
    private static final By BACK_HOME_BUTTON = By.cssSelector("[data-test='back-to-products']");

    public CheckoutPage(WebDriver driver) {
        super(driver);
    }

    // Types all fields then submits the form via JavaScript form.submit()
    // This is the most reliable approach in headless Chrome Docker environments
    // where <input type="submit"> click events don't always fire form submission
    public CheckoutPage fillInfo(String firstName, String lastName, String postalCode) {
        type(FIRST_NAME_INPUT, firstName);
        type(LAST_NAME_INPUT, lastName);
        type(POSTAL_CODE, postalCode);
        // Use JavaScript to click the button - most reliable in headless environment
        JavascriptExecutor js = (JavascriptExecutor) driver;
        WebElement button = driver.findElement(CONTINUE_BUTTON);
        js.executeScript("arguments[0].click();", button);
        
        // CRITICAL: Add 2-second delay for form submission to complete in headless Docker environment
        // The form submission is asynchronous and headless Chrome requires significant wait time
        // Previous 1-second delay was insufficient - increasing to 2 seconds for reliability
        try {
            long startTime = System.currentTimeMillis();
            Thread.sleep(2000);  // 2 second delay - REQUIRED for headless Chrome form processing
            long elapsedTime = System.currentTimeMillis() - startTime;
            System.out.println("✅ Form submission delay completed: " + elapsedTime + "ms");
        } catch (InterruptedException e) {
            System.out.println("⚠️ Form submission delay was interrupted!");
            Thread.currentThread().interrupt();
        }
        
        waitForUrlToContain("checkout-step-two");
        return this;
    }

    // Submits empty form — stays on step 1 with validation error
    // Uses jsClick since empty form won't navigate away
    public CheckoutPage submitEmptyForm() {
        jsClick(CONTINUE_BUTTON);
        waitForElementVisible(ERROR_MESSAGE);
        return this;
    }

    // Types only first name — triggers last name required error
    public CheckoutPage fillFirstNameOnly(String firstName) {
        type(FIRST_NAME_INPUT, firstName);
        jsClick(CONTINUE_BUTTON);
        waitForElementVisible(ERROR_MESSAGE);
        return this;
    }

    public String getErrorMessage() {
        return getText(ERROR_MESSAGE);
    }

    public boolean isErrorDisplayed() {
        return isVisible(ERROR_MESSAGE);
    }

    public String getItemTotal() {
        return getText(ITEM_TOTAL);
    }

    // jsClick on Finish too — same Docker headless reliability
    public CheckoutPage finishOrder() {
        jsClick(FINISH_BUTTON);
        waitForElementPresent(COMPLETE_HEADER);
        return this;
    }

    public String getConfirmationMessage() {
        return getText(COMPLETE_HEADER);
    }

    public boolean isOrderComplete() {
        return isVisible(COMPLETE_HEADER);
    }

    public InventoryPage backToProducts() {
        jsClick(BACK_HOME_BUTTON);
        waitForUrlToContain("inventory");
        return new InventoryPage(driver);
    }
}