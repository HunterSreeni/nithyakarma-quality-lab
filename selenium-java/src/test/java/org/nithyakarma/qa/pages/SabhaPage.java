package org.nithyakarma.qa.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

public class SabhaPage extends BasePage {
    private final By heading = By.cssSelector("main h1");
    // "This week · resets Sunday night" / "This month"
    private final By periodLine = By.cssSelector("main h1 + *");
    // React renders "{name} (You)" as two text nodes, so match any of them
    private final By ownRow = By.xpath("//main//*[text()[contains(., '(You)')]]");

    public SabhaPage(WebDriver driver, WebDriverWait wait) {
        super(driver, wait);
    }

    public void open(String baseUrl) {
        driver.get(baseUrl + "/sabha");
        waitForText(heading, "Sabha Leaderboard");
    }

    public String heading() { return textOf(heading); }
    public String periodLine() { return textOf(periodLine); }
    public boolean isOwnRowVisible() { return visible(ownRow).isDisplayed(); }

    // Week / Month / Kids segmented buttons; the active one gets class "on"
    private By segment(String name) {
        return By.xpath("//button[contains(@class,'seg') and normalize-space()='" + name + "']");
    }

    public boolean isSegmentOn(String name) {
        return visible(segment(name)).getDomAttribute("class").matches(".*\\bon\\b.*");
    }

    public void select(String name) {
        visible(segment(name)).click();
        wait.until(d -> isSegmentOn(name));
    }

    public void waitForHeading(String expected) { waitForText(heading, expected); }
    public void waitForPeriod(String regex) { waitForText(periodLine, regex); }
}
