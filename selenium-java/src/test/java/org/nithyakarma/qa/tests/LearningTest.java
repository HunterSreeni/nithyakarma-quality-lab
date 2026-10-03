package org.nithyakarma.qa.tests;

import org.assertj.core.api.SoftAssertions;
import org.nithyakarma.qa.base.LoggedInBaseTest;
import org.nithyakarma.qa.pages.LearningPage;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

// mirrors playwright-ts/tests/app/learning.spec.ts
public class LearningTest extends LoggedInBaseTest {
    private LearningPage learning;

    @BeforeMethod
    public void openLearning() {
        learning = new LearningPage(driver, wait);
        learning.open(baseUrl);
    }

    @Test
    public void listsEveryReadAlongPractice() {
        assertThat(learning.practiceLinkCount()).isEqualTo(LearningPage.PRACTICES.size());
        SoftAssertions softly = new SoftAssertions();
        for (String name : LearningPage.PRACTICES) {
            softly.assertThat(learning.practiceLink(name).isDisplayed()).as(name).isTrue();
        }
        softly.assertAll();
    }

    @Test
    public void openingHanumanChalisaShowsReaderWithLanguageSwitch() {
        learning.practiceLink("Hanuman Chalisa").click();
        wait.until(ExpectedConditions.urlMatches("/learning/hanuman-chalisa$"));
        assertThat(learning.detailHeading()).isEqualTo("Hanuman Chalisa");
        assertThat(learning.isYoutubeLinkVisible()).isTrue();

        assertThat(learning.isLanguagePressed("English")).isTrue();
        learning.languageButton("Sanskrit").click();
        wait.until(d -> learning.isLanguagePressed("Sanskrit"));
        assertThat(learning.isLanguagePressed("English")).isFalse();
    }
}
