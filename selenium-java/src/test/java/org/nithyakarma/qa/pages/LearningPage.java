package org.nithyakarma.qa.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.util.List;

public class LearningPage extends BasePage {
    public static final List<String> PRACTICES = List.of(
        "Hanuman Chalisa", "Vishnu Sahasranamam", "Sai Baba Aarti", "Lalitha Sahasranamam",
        "Soundarya Lahari", "Ramayanam", "Devi Mahatmyam", "Dakshinamurthy Stotram",
        "Aditya Hrudayam", "Subrahmanya Bhujangam", "Mukundamala", "Sri Rudram",
        "Sandhyavandhanam", "Samidhadhanam");

    private final By heading = By.xpath("//main//h1[normalize-space()='Read along']");
    private final By practiceLinks = By.cssSelector("main a[href^='/learning/']");
    private final By youtubeLink = By.xpath("//main//a[contains(., 'Watch on YouTube')]");

    public LearningPage(WebDriver driver, WebDriverWait wait) {
        super(driver, wait);
    }

    public void open(String baseUrl) {
        driver.get(baseUrl + "/learning");
        visible(heading);
    }

    public int practiceLinkCount() {
        visible(practiceLinks);
        return driver.findElements(practiceLinks).size();
    }

    public WebElement practiceLink(String name) {
        return visible(By.xpath("//main//a[starts-with(@href,'/learning/')][.//*[normalize-space(text())='" + name + "']]"));
    }

    // detail page (/learning/<slug>)
    public String detailHeading() { return textOf(By.cssSelector("main h1")); }
    public boolean isYoutubeLinkVisible() { return visible(youtubeLink).isDisplayed(); }

    public WebElement languageButton(String name) {
        return visible(By.xpath("//*[@role='group' and @aria-label='Language']//button[normalize-space()='" + name + "']"));
    }

    public boolean isLanguagePressed(String name) {
        return "true".equals(languageButton(name).getDomAttribute("aria-pressed"));
    }
}
