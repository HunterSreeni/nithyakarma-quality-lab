package org.nithyakarma.qa.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.util.LinkedHashMap;
import java.util.Map;

// top bar shared by every logged-in page
public class AppHeader extends BasePage {
    // tab name -> url path
    public static final Map<String, String> TABS = new LinkedHashMap<>();
    static {
        TABS.put("Today", "/");
        TABS.put("Learning", "/learning");
        TABS.put("History", "/history");
        TABS.put("Sabha", "/sabha");
        TABS.put("Calendar", "/calendar");
        TABS.put("Profile", "/profile");
    }

    private final By logo = By.cssSelector("header img[alt='Nithyakarma']");
    // flame + number, no label/role so CSS is the only handle
    private final By streakPill = By.cssSelector(".streak-pill");
    private final By logoutButton = By.cssSelector("button.nav-logout");

    public AppHeader(WebDriver driver, WebDriverWait wait) {
        super(driver, wait);
    }

    public WebElement tab(String name) {
        return visible(By.xpath("//nav[@aria-label='Primary']/a[normalize-space()='" + name + "']"));
    }

    public boolean isLogoVisible() {
        return visible(logo).isDisplayed();
    }

    public String streakPillText() {
        return textOf(streakPill);
    }

    public boolean isLogoutVisible() {
        return isPresent(logoutButton) && driver.findElement(logoutButton).isDisplayed();
    }

    // the app renders the header once the session loads
    public void waitUntilLoggedIn() {
        visible(logoutButton);
    }

    public void logout() {
        visible(logoutButton).click();
    }

    public void goTo(String name) {
        tab(name).click();
        String path = TABS.get(name);
        wait.until(ExpectedConditions.urlMatches(path.equals("/") ? "/$" : path + "$"));
        // aria-current moves a render after the url changes
        wait.until(d -> "page".equals(tab(name).getDomAttribute("aria-current")));
    }
}
