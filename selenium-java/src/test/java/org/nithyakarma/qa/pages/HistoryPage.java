package org.nithyakarma.qa.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.util.List;

public class HistoryPage extends BasePage {
    // each history row starts with a date like "Tue, 18 Aug, 2026"
    private static final String ENTRY_DATE = "[A-Z][a-z]{2}, \\d{2} [A-Z][a-z]{2}, \\d{4}";

    private final By heading = By.xpath("//main//h1[normalize-space()='History']");
    private final By meButton = By.xpath("//main//button[substring(normalize-space(.), string-length(normalize-space(.)) - 1) = 'Me']");
    private final By addChildButton = By.xpath("//main//button[contains(., 'Add child')]");
    private final By leafDivs = By.xpath("//main//div[not(*)]");

    public HistoryPage(WebDriver driver, WebDriverWait wait) {
        super(driver, wait);
    }

    public void open(String baseUrl) {
        driver.get(baseUrl + "/history");
        visible(heading);
    }

    public boolean isMeVisible() { return visible(meButton).isDisplayed(); }
    public boolean isAddChildVisible() { return visible(addChildButton).isDisplayed(); }

    public List<String> entryDates() {
        // rows load after the heading, wait for the first date
        wait.until(d -> d.findElements(leafDivs).stream().anyMatch(e -> text(e).matches(ENTRY_DATE)));
        return driver.findElements(leafDivs).stream().map(BasePage::text).filter(t -> t.matches(ENTRY_DATE)).toList();
    }
}
