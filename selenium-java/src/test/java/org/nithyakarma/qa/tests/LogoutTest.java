package org.nithyakarma.qa.tests;

import org.nithyakarma.qa.base.LoggedInBaseTest;
import org.nithyakarma.qa.pages.AppHeader;
import org.nithyakarma.qa.support.Session;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

// mirrors playwright-ts/tests/app/logout.spec.ts
// own fresh session, and testng.xml runs it last: logging out can revoke the user's other sessions
public class LogoutTest extends LoggedInBaseTest {

    // TestNG only treats an override as setup if it is annotated again
    @Override
    @BeforeMethod
    public void logIn() {
        openWithSession(Session.createSessionJson());
    }

    @Test
    public void logoutReturnsToLoginPage() {
        AppHeader header = new AppHeader(driver, wait);
        header.waitUntilLoggedIn();

        header.logout();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[normalize-space()='Welcome back']")));
        assertThat(header.isLogoutVisible()).isFalse();
    }
}
