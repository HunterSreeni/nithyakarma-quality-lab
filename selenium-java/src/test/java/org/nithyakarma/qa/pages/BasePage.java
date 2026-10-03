package org.nithyakarma.qa.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

// shared helpers for the logged-in pages (LoginPage predates this and stays as is)
public abstract class BasePage {
    protected final WebDriver driver;
    protected final WebDriverWait wait;

    protected BasePage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    protected WebElement visible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected boolean isPresent(By locator) {
        return !driver.findElements(locator).isEmpty();
    }

    // textContent, not getText(): getText() returns the CSS-uppercased screen text
    public static String text(WebElement element) {
        String value = element.getDomProperty("textContent");
        return value == null ? "" : value.replaceAll("\\s+", " ").trim();
    }

    // waits for non-blank text: React often renders the element first and fills it in later
    protected String textOf(By locator) {
        visible(locator);
        return wait.until(d -> {
            var found = d.findElements(locator);
            String value = found.isEmpty() ? "" : text(found.get(0));
            return value.isBlank() ? null : value;
        });
    }

    // wait until the element's text matches a regex (React updates after clicks)
    protected void waitForText(By locator, String regex) {
        wait.until(d -> {
            var found = d.findElements(locator);
            return !found.isEmpty() && text(found.get(0)).matches(regex);
        });
    }

    protected static By buttonWithText(String exactText) {
        return By.xpath("//button[normalize-space()='" + exactText + "']");
    }
}
