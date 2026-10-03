package org.nithyakarma.qa.pages;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.bidi.module.Network;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.TimeoutException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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

    public void selectChip(String text) {
        clickAndWaitForSave(() -> visible(chip(text)).click());
        wait.until(d -> isChipOn(text));
    }

    // The app updates the screen first and saves in the background; reloading before the
    // save is sent cancels it. WebDriver BiDi (Selenium 4) lets us listen to the network,
    // the same idea as Playwright's waitForResponse: click, then wait for the PATCH reply.
    // A latch, not WebDriverWait: WebDriverWait polls every 500ms, which hid the event
    // (~530ms per save vs ~230ms with the latch; Playwright's waitForResponse ~210ms).
    private void clickAndWaitForSave(Runnable click) {
        CountDownLatch saved = new CountDownLatch(1);
        try (Network network = new Network(driver)) {
            network.onResponseCompleted(response -> {
                if ("PATCH".equals(response.getRequest().getMethod())
                        && response.getRequest().getUrl().contains("/rest/v1/profiles")
                        && response.getResponseData().getStatus() / 100 == 2) {
                    saved.countDown();
                }
            });
            click.run();
            if (!saved.await(10, TimeUnit.SECONDS)) {
                throw new TimeoutException("no successful PATCH /rest/v1/profiles within 10s");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
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
        if (box.isSelected() != checked) clickAndWaitForSave(box::click);
        wait.until(d -> isLeaderboardOptedIn() == checked);
    }
}
