package org.nithyakarma.qa.tests;


import org.testng.annotations.Test;
import org.nithyakarma.qa.base.BaseTest;
import org.nithyakarma.qa.pages.LoginPage;
import org.testng.annotations.DataProvider;
import static org.assertj.core.api.Assertions.assertThat;



public class AuthTest extends BaseTest {
    @Test
    public void rendersAllAuthPageElements() {
        LoginPage loginPage = new LoginPage(driver, wait);
        loginPage.open(baseUrl);
        loginPage.checkLoginElements();    
    }
    // login form validations
    @DataProvider(name = "validationCases")
    public Object[][] validationCases() {
        return new Object[][] {
            // name,                field,      value,           expected
            { "bad email format",       "email",    "notanemail",    "typeMismatch" },
            { "email missing domain",   "email",    "user@",         "typeMismatch" },
            { "password 7 chars",       "password", "1234567",       "tooShort" },
            { "valid email",            "email",    "a@b.co",        "valid" },
            { "8 char password",        "password", "12345678",      "valid" },
            { "empty email",            "email",    "",              "valueMissing" },
            { "empty password",         "password", "",              "valueMissing" },
        };
    }
    @Test(dataProvider = "validationCases")
    public void validation(String name, String field, String value, String expected) {
        LoginPage loginPage = new LoginPage(driver, wait);
        loginPage.open(baseUrl);
        loginPage.fillField(field, value);
        assertThat(loginPage.fieldValidation(field)).as(name).isEqualTo(expected);
    }

    @Test
    public void logInWithValidCredentials() throws InterruptedException {
        String email = ENV.get("E2E_EMAIL");
        String password = ENV.get("E2E_PASSWORD");
        if (email == null || password == null) throw new IllegalStateException("E2E_EMAIL / E2E_PASSWORD missing from .env");
        LoginPage loginPage = new LoginPage(driver, wait);
        loginPage.open(baseUrl);
        loginPage.fillCredentials(email, password);
        // negative test cases assertion for captcha block
        loginPage.solveCaptcha();
    }

}
