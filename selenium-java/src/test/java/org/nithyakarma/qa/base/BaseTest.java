package org.nithyakarma.qa.base;

import io.github.cdimascio.dotenv.Dotenv;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import java.time.Duration;

public class BaseTest {
    // read the root .env once; ".." because mvn runs from selenium-java/
    // ignoreIfMissing: CI has no .env, the values come in as environment variables
    protected static final Dotenv ENV = Dotenv.configure().directory("..").ignoreIfMissing().load();

    protected WebDriver driver;
    protected WebDriverWait wait;
    protected String baseUrl = ENV.get("BASE_URL");

    @BeforeMethod
    public void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        // desktop layout (same viewport as Playwright's Desktop Chrome)
        options.addArguments("--window-size=1280,720");
        // WebDriver BiDi: lets tests listen to network events (ProfilePage waits for saves)
        options.enableBiDi();
        // BiDi sessions dismiss confirm() popups on their own; leave them for the test to handle
        options.setUnhandledPromptBehaviour(UnexpectedAlertBehaviour.IGNORE);
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
