package org.nithyakarma.qa.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TodayPage extends BasePage {
    // "Saturday, 03 October 2026"
    private final By dateLine = By.cssSelector("main .eyebrow");
    private final By greeting = By.cssSelector("main h1.greet");
    private final By progressLine = By.cssSelector("main .greet-sub");
    // panchangam + kalams have no roles, the app's class names are the stable handle
    private final By panchangamBox = By.cssSelector(".panchangam-box");
    private final By kalams = By.cssSelector(".pb-kalam");
    private final By meButton = By.xpath("//main//button[substring(normalize-space(.), string-length(normalize-space(.)) - 1) = 'Me']");
    private final By addChildButton = By.xpath("//main//button[contains(., 'Add child')]");
    private final By streakCard = By.xpath("//main//*[normalize-space(text())='Current Streak']/../..");
    private final By anushtanamsHeading = By.xpath("//main//h2[contains(., 'Anushtanams')]");
    private final By practiceCards = By.cssSelector(".practice-card");
    private final By sandhyaProgress = By.cssSelector(".sandhya-progress");
    private final By addAnushtanamToggle = By.xpath("//button[contains(., 'Add an anushtanam to track')]");
    private final By pickerSearch = By.cssSelector("input.dd-search");
    private final By pickerOptions = By.cssSelector(".dropdown button.dd-item");
    // Morning asks for the Gayatri japam count before saving
    private final By countDialog = By.cssSelector("[role='dialog']");

    public TodayPage(WebDriver driver, WebDriverWait wait) {
        super(driver, wait);
    }

    public void open(String baseUrl) {
        driver.get(baseUrl);
        visible(greeting);
    }

    // "Saturday, 03 October 2026" for today in IST
    public static String expectedDateLine() {
        return ZonedDateTime.now(ZoneId.of("Asia/Kolkata"))
            .format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", Locale.ENGLISH));
    }

    public String dateLine() { return textOf(dateLine); }
    public String greeting() { return textOf(greeting); }
    public String progressLine() { return textOf(progressLine); }
    public String panchangamText() { return textOf(panchangamBox); }

    // kalam name -> "HH:MM-HH:MM"
    public Map<String, String> kalamTimes() {
        visible(kalams);
        Map<String, String> times = new LinkedHashMap<>();
        for (WebElement kalam : driver.findElements(kalams)) {
            times.put(text(kalam.findElement(By.cssSelector(".pb-kalam-name"))),
                      text(kalam.findElement(By.cssSelector(".pb-kalam-time"))));
        }
        return times;
    }

    public boolean isMeVisible() { return visible(meButton).isDisplayed(); }
    public boolean isAddChildVisible() { return visible(addChildButton).isDisplayed(); }
    public void clickAddChild() { visible(addChildButton).click(); }
    public String streakCardText() { return textOf(streakCard); }
    public boolean isAnushtanamsHeadingVisible() { return visible(anushtanamsHeading).isDisplayed(); }

    public int practiceCardCount() {
        visible(practiceCards);
        return driver.findElements(practiceCards).size();
    }

    public boolean hasPracticeCard(String name) {
        return isPresent(By.xpath("//div[contains(@class,'practice-card')]//*[contains(@class,'p-name') and normalize-space()='" + name + "']"));
    }

    public boolean isMarkDoneVisible(String practiceName) {
        return visible(By.xpath("//div[contains(@class,'practice-card')][.//*[contains(@class,'p-name') and normalize-space()='"
            + practiceName + "']]//button[normalize-space()='Mark Done']")).isDisplayed();
    }

    public WebElement slotButton(String slot) {
        return visible(By.xpath("//button[contains(@class,'slot-btn') and normalize-space()='" + slot + "']"));
    }

    public String sandhyaProgress() { return textOf(sandhyaProgress); }

    public int sandhyasDone() {
        Matcher m = Pattern.compile("(\\d) of 3").matcher(sandhyaProgress());
        if (!m.find()) throw new IllegalStateException("no sandhya progress text");
        return Integer.parseInt(m.group(1));
    }

    // clicks Morning, checks the count dialog defaults to 108, saves; returns that default
    public String markMorningSandhya() {
        slotButton("Morning").click();
        WebElement dialog = visible(countDialog);
        String defaultCount = dialog.findElement(By.cssSelector("input")).getDomProperty("value");
        dialog.findElement(By.xpath(".//button[normalize-space()='Save']")).click();
        waitForText(sandhyaProgress, "1 of 3 sandhyas done.*");
        return defaultCount;
    }

    public void openPicker() {
        visible(addAnushtanamToggle).click();
        visible(pickerSearch);
        // a muted "Loading..." row shows first, the options are buttons
        visible(pickerOptions);
    }

    public List<WebElement> pickerOptions() {
        return driver.findElements(pickerOptions);
    }

    public WebElement pickerOption(String name) {
        return visible(By.xpath("//div[contains(@class,'dropdown')]//button[.//*[contains(@class,'dd-name') and normalize-space()='" + name + "']]"));
    }

    public static String optionName(WebElement option) {
        return text(option.findElement(By.cssSelector(".dd-name")));
    }

    public void search(String term) {
        visible(pickerSearch).sendKeys(term);
    }

    public void waitForPickerCount(int count) {
        wait.until(ExpectedConditions.numberOfElementsToBe(pickerOptions, count));
    }

    public void waitForPracticeCard(String name) {
        wait.until(d -> hasPracticeCard(name));
    }
}
