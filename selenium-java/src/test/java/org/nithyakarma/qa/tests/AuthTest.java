package org.nithyakarma.qa.tests;


import org.testng.annotations.Test;
import org.nithyakarma.qa.base.BaseTest;
import org.nithyakarma.qa.pages.LoginPage;


public class AuthTest extends BaseTest {
    @Test
    public void rendersAllAuthPageElements() {
        LoginPage loginPage = new LoginPage(driver, wait);
        loginPage.open(baseUrl);
        loginPage.checkLoginElements();
    }
}
