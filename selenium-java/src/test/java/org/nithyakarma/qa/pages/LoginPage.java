package org.nithyakarma.qa.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.assertj.core.api.SoftAssertions;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import org.openqa.selenium.JavascriptExecutor;

public class LoginPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By logo = By.cssSelector("img[alt='Nithyakarma']");
    //email and password fields
    private final By emailInput = By.id("auth-email");
    private final By passwordInput = By.id("auth-password");
    // cloudflare captcha elements
    private final By cloudFlare = By.cssSelector("div.turnstile-widget");
    // DEV-NOTE - login button shows 'verifying...' until the captcha is resolved
    private final By loginButton = By.xpath("//button[normalize-space()='Verifying...']");
    private final By signIn = By.xpath("//button[normalize-space()='Sign In']");


    public LoginPage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    public void open(String baseUrl) {
        driver.get(baseUrl);
        // 1. hard gate - page loaded or timeout exception ends the test
        wait.until(ExpectedConditions.visibilityOfElementLocated(logo));
    }

    public void checkLoginElements() {
        // 2. UI elements check group 1 is welcome back and form fields non-buttons items
        // what to check: description -> locator
        Map<String, By> elements = new LinkedHashMap<>();
        elements.put("hero image", By.cssSelector("img[alt='Periyava']"));
        elements.put("email field", By.id("auth-email"));
        elements.put("password", By.id("auth-password"));
        elements.put("captcha", By.cssSelector("div.turnstile-widget"));

        // 3. buttons check
        // buttons: description -> accessible name
        Map<String, String> buttonNames = new LinkedHashMap<>();
        buttonNames.put("Google SSO", "Continue with Google");
        buttonNames.put("forgot password", "Forgot password?");
        buttonNames.put("login", "Verifying...");
        buttonNames.put("signup", "Create account");

        SoftAssertions softly = new SoftAssertions();

        // loop over elements
        for (Map.Entry<String, By> e : elements.entrySet()) {
            softly.assertThat(driver.findElements(e.getValue()))
                .as(e.getKey())
                .anyMatch(WebElement::isDisplayed);
        }
        // loop over buttons
        List<WebElement> buttons = driver.findElements(By.tagName("button"));
        for (Map.Entry<String, String> e : buttonNames.entrySet()) {
            softly.assertThat(buttons)
                .as(e.getKey())
                .anyMatch(b -> b.getAccessibleName().contains(e.getValue()) && b.isDisplayed());
        }

        // one-offs that check text
        softly.assertThat(driver.findElements(By.tagName("h1")))
            .as("welcome heading")
            .anyMatch(h -> h.getText().equals("Welcome back") && h.isDisplayed());
        softly.assertThat(driver.findElements(By.cssSelector("div.auth-agree")))
            .as("terms text")
            .anyMatch(t -> t.getText().contains("By continuing you agree to our") && t.isDisplayed());
        
        // report all at once
        softly.assertAll();
    }
        
        // empty field validations
        public String fieldValidation(String field) {
            By locator = field.equals("email") ? emailInput : passwordInput;
            WebElement el = driver.findElement(locator);
            return (String) ((JavascriptExecutor) driver).executeScript(
                "const v = arguments[0].validity;" +
                "if (v.valueMissing) return 'valueMissing';" +
                "if (v.typeMismatch) return 'typeMismatch';" +
                "if (v.tooShort) return 'tooShort';" +
                "return v.valid ? 'valid': 'other';", el);
        }
        
        // email and password filling
        public void fillField(String field, String value) {
            By locator = field.equals("email") ? emailInput : passwordInput;
            driver.findElement(locator).sendKeys(value);
        }

        public void fillCredentials(String email, String password) {
            driver.findElement(emailInput).sendKeys(email);
            driver.findElement(passwordInput).sendKeys(password);
        }

        //wait for the captcha, click it then check if it still blocks bots
        public void solveCaptcha() throws InterruptedException {
            // fixed 6 seconds wait
            Thread.sleep(6000);
            // if the captcha miraculously passes
            if (driver.findElements(signIn).isEmpty()) {
                //click checkbox if signin is not visible
                driver.findElement(cloudFlare).click();
                Thread.sleep(6000);
            }
            //negative testing of the captcha failing for bots
            wait.until(ExpectedConditions.visibilityOfElementLocated(loginButton));
    }
}
