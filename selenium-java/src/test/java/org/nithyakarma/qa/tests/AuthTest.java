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

            WebElement heroImg = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("img[alt='Periyava']"))
            );
        
            assertThat(heroImg.isDisplayed()).as("hero image").isTrue();
            } finally {
                driver.quit();
            }
    }
}
