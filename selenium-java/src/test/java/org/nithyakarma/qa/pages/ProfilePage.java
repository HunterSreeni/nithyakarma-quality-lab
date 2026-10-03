package org.nithyakarma.qa.pages;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.util.Map;

public class ProfilePage extends BasePage {
    private final By name = By.cssSelector("main h1");
    private final By tierProgress = By.xpath("//main//*[contains(text(), 'punya points')]");
    private final By displayNameInput = By.id("pf-name");
    private final By saveButton = buttonWithText("Save changes");
    private final By familyHeading = By.xpath("//main//h2[starts-with(normalize-space(), 'Family Members')]");
    private final By addFamilyMemberButton = By.xpath("//button[contains(., 'Add family member')]");
    private final By childNameInput = By.id("fam-name");
    private final By addChildSubmit = By.xpath("//form//button[normalize-space()='Add']");
    private final By inviteCode = By.xpath("//h2[normalize-space()='Invite & earn rewards']/..//strong");
    private final By leaderboardOptIn = By.xpath("//label[contains(., 'Show me on community leaderboards')]/input");
    private final By deleteConfirmInput = By.xpath("//h2[normalize-space()='Danger zone']/..//input");
    private final By deleteAccountButton = buttonWithText("Delete my account & all data");

    public ProfilePage(WebDriver driver, WebDriverWait wait) {
        super(driver, wait);
    }

    // the app paints its cached profile first and the server copy ~0.3s later
    private static final String CACHE_KEY = "nk_profile_cache_v1";

    public void open(String baseUrl) {
        driver.get(baseUrl + "/profile");
        freshLoad();
    }

    // drop the cache, reload, and wait until the app has rewritten it from the server
    public void freshLoad() {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("localStorage.removeItem(arguments[0]);", CACHE_KEY);
        driver.navigate().refresh();
        wait.until(d -> js.executeScript("return localStorage.getItem(arguments[0]);", CACHE_KEY) != null);
        visible(displayNameInput);
    }

    public String name() { return textOf(name); }
    public String tierProgress() { return textOf(tierProgress); }
    public String inviteCode() { return textOf(inviteCode); }
    public boolean isFamilySectionVisible() { return visible(familyHeading).isDisplayed(); }

    // Streak / Best / Punya number tiles
    public String stat(String label) {
        return textOf(By.xpath("//main//*[normalize-space(text())='" + label + "']/.."));
    }

    public String displayName() { return visible(displayNameInput).getDomProperty("value"); }

    // select-all + type, so React sees a real change event (clear() alone doesn't)
    public void typeDisplayName(String value) {
        visible(displayNameInput).sendKeys(Keys.chord(Keys.CONTROL, "a"), value);
    }

    public boolean isSaveEnabled() { return visible(saveButton).isEnabled(); }

    public void waitForSaveEnabled(boolean enabled) {
        wait.until(d -> isSaveEnabled() == enabled);
    }

    public boolean isDeleteEnabled() { return visible(deleteAccountButton).isEnabled(); }
    public void typeDeleteConfirmation(String value) { visible(deleteConfirmInput).sendKeys(value); }

    public String footerLinkHref(String text) {
        return visible(By.xpath("//main//a[normalize-space()='" + text + "']")).getDomAttribute("href");
    }

    // radio-chip buttons (Tamil/Malayalam, Bachelor/Married); the chosen one has class "on"
    private By chip(String text) {
        return By.xpath("//button[contains(@class,'radio-chip') and normalize-space()='" + text + "']");
    }

    public boolean isChipOn(String text) {
        return visible(chip(text)).getDomAttribute("class").matches(".*\\bon\\b.*");
    }

    // which profile column each settings chip writes, and the value it saves
    private static final Map<String, Object[]> CHIP_FIELDS = Map.of(
        "Tamil", new Object[] { "panchangam_tradition", "tamil" },
        "Malayalam", new Object[] { "panchangam_tradition", "malayalam" },
        "Bachelor", new Object[] { "is_married", false },
        "Married", new Object[] { "is_married", true });

    public void selectChip(String text) {
        Object[] field = CHIP_FIELDS.get(text);
        clickAndWaitForSave(() -> visible(chip(text)).click(), (String) field[0], field[1]);
        wait.until(d -> isChipOn(text));
    }

    // The app updates the screen first, saves in the background, then re-fetches the
    // profile and rewrites its cache. Reloading before the save is sent cancels it.
    // Selenium has no network hooks, so: clear the cache, click, and wait for the cache
    // to come back holding the new value - that's the "save finished" signal.
    private void clickAndWaitForSave(Runnable click, String field, Object expected) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("localStorage.removeItem(arguments[0]);", CACHE_KEY);
        click.run();
        wait.until(d -> Boolean.TRUE.equals(js.executeScript(
            "const cached = localStorage.getItem(arguments[0]);" +
            "return cached !== null && JSON.parse(cached).profile[arguments[1]] === arguments[2];",
            CACHE_KEY, field, expected)));
    }

    private By childRow(String childName) {
        return By.xpath("//div[contains(@class,'fam-row')][.//*[contains(@class,'fam-name') and normalize-space()='" + childName + "']]");
    }

    public boolean hasChild(String childName) { return isPresent(childRow(childName)); }
    public String childRowText(String childName) { return textOf(childRow(childName)); }

    public void addChild(String childName, String gender) {
        visible(addFamilyMemberButton).click();
        visible(childNameInput).sendKeys(childName);
        visible(chip(gender)).click();
        visible(addChildSubmit).click();
        visible(childRow(childName));
    }

    // Remove asks a native confirm(): "Remove <name> and all their logs?"
    public String removeChild(String childName) {
        visible(childRow(childName)).findElement(By.cssSelector("button.fam-remove")).click();
        Alert confirm = wait.until(ExpectedConditions.alertIsPresent());
        String message = confirm.getText();
        confirm.accept();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(childRow(childName)));
        return message;
    }

    public boolean isLeaderboardOptedIn() {
        return driver.findElement(leaderboardOptIn).isSelected();
    }

    public void setLeaderboardOptIn(boolean checked) {
        WebElement box = wait.until(ExpectedConditions.presenceOfElementLocated(leaderboardOptIn));
        if (box.isSelected() != checked) clickAndWaitForSave(box::click, "leaderboard_opt_in", checked);
        wait.until(d -> isLeaderboardOptedIn() == checked);
    }
}
