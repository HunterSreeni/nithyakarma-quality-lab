package org.nithyakarma.qa.tests;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import java.time.Duration;
import org.testng.annotations.Test;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import static org.assertj.core.api.Assertions.assertThat;
import org.openqa.selenium.chrome.ChromeOptions;
import org.assertj.core.api.SoftAssertions;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;


public class AuthTest {
    @Test
    public void authTest() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.setBinary("/usr/bin/chromium");
        WebDriver driver = new ChromeDriver(options);
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            driver.get("https://app.nithyakarma.org");

            // 1. hard gate - page loaded or timeout exception ends the test
            WebElement heroImg = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("img[alt='Nithyakarma']"))
            );
            // 2. UI elements check group 1 is welcome back and form fields non-buttons items
            // what to check: description -> locator
            Map<String, By> elements = new LinkedHashMap<>();
            elements.put("logo image", By.cssSelector("img[alt='Periyava']"));
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
        
            // assertThat(heroImg.isDisplayed()).as("hero image").isTrue();
            } finally {
                driver.quit();
            }
    }
}
