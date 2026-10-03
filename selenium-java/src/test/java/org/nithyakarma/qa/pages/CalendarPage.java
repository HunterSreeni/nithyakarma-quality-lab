package org.nithyakarma.qa.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

public class CalendarPage extends BasePage {
    private final By heading = By.xpath("//main//h1[normalize-space()='Panchangam']");
    // "புரட்டாசி 17 / Purattasi 17" next to Previous/Next
    private final By periodTitle = By.cssSelector(".cal-title");
    private final By previousButton = By.cssSelector("button[aria-label='Previous']");
    private final By nextButton = By.cssSelector("button[aria-label='Next']");
    private final By kalamsHeading = By.xpath("//main//h2[normalize-space()='Kalams to avoid']");

    public CalendarPage(WebDriver driver, WebDriverWait wait) {
        super(driver, wait);
    }

    public void open(String baseUrl) {
        driver.get(baseUrl + "/calendar");
        visible(heading);
    }

    public String periodTitle() { return textOf(periodTitle); }
    public void next() { visible(nextButton).click(); }
    public void previous() { visible(previousButton).click(); }
    public boolean isKalamsHeadingVisible() { return visible(kalamsHeading).isDisplayed(); }

    public void waitForTitleNot(String title) {
        wait.until(d -> !periodTitle().equals(title));
    }

    public void waitForTitle(String title) {
        wait.until(d -> periodTitle().equals(title));
    }

    private By viewButton(String name) {
        return By.xpath("//*[@role='group' and @aria-label='Calendar view']//button[normalize-space()='" + name + "']");
    }

    public void selectView(String name) {
        visible(viewButton(name)).click();
        wait.until(d -> isViewPressed(name));
    }

    public boolean isViewPressed(String name) {
        return "true".equals(visible(viewButton(name)).getDomAttribute("aria-pressed"));
    }

    // label rows inside the Panchangam section (Thithi, Nakshatram, Varsham)
    public boolean isDetailVisible(String label) {
        return visible(By.xpath("//main//*[normalize-space(text())='" + label + "']")).isDisplayed();
    }

    public String kalamText(String name) {
        return textOf(By.xpath("//main//*[normalize-space(text())='" + name + "']/.."));
    }
}
