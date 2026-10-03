package org.nithyakarma.qa.base;

import org.nithyakarma.qa.support.Session;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.BeforeMethod;
import java.util.Map;

// BaseTest opens Chrome (TestNG runs the parent @BeforeMethod first), then this logs in
public class LoggedInBaseTest extends BaseTest {

    @BeforeMethod
    public void logIn() {
        // the app shows IST dates/kalams; pin it so CI (UTC) matches
        ((ChromeDriver) driver).executeCdpCommand("Emulation.setTimezoneOverride", Map.of("timezoneId", "Asia/Kolkata"));
        openWithSession(Session.sharedSessionJson());
    }

    protected void openWithSession(String sessionJson) {
        driver.get(baseUrl);
        ((JavascriptExecutor) driver).executeScript(
            "localStorage.setItem(arguments[0], arguments[1]);", Session.storageKey(), sessionJson);
        driver.navigate().refresh();
    }
}
