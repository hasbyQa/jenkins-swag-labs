package com.swaglabs.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

// Represents the Shopping Cart page (/cart.html)
public class CartPage extends BasePage {

    private static final By PAGE_TITLE      = By.cssSelector("[data-test='title']");
    private static final By CART_ITEM       = By.cssSelector("[data-test='inventory-item']");
    private static final By ITEM_NAME       = By.cssSelector("[data-test='inventory-item-name']");
    private static final By CHECKOUT_BUTTON = By.cssSelector("[data-test='checkout']");
    private static final By CONTINUE_BUTTON = By.cssSelector("[data-test='continue-shopping']");
    private static final By REMOVE_BUTTON   = By.cssSelector("[data-test^='remove']");

    // Confirmed from live DOM: camelCase, visible immediately after checkout click
    private static final By CHECKOUT_FIRST_NAME = By.cssSelector("[data-test='firstName']");

    public CartPage(WebDriver driver) {
        super(driver);
    }

    public boolean isLoaded() {
        return isVisible(PAGE_TITLE);
    }

    public boolean hasItems() {
        return isVisible(CART_ITEM);
    }

    public String getFirstItemName() {
        return getText(ITEM_NAME);
    }

    public CartPage removeFirstItem() {
        click(REMOVE_BUTTON);
        return this;
    }

    // Clicks Checkout and waits for the firstName field to confirm step-one is rendered
    public CheckoutPage proceedToCheckout() {
        click(CHECKOUT_BUTTON);
        waitForElementVisible(CHECKOUT_FIRST_NAME);
        return new CheckoutPage(driver);
    }

    public InventoryPage continueShopping() {
        jsClick(CONTINUE_BUTTON);
        waitForUrlToContain("inventory");
        return new InventoryPage(driver);
    }
}