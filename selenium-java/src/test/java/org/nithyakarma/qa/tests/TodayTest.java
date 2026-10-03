package org.nithyakarma.qa.tests;

import org.assertj.core.api.SoftAssertions;
import org.nithyakarma.qa.base.LoggedInBaseTest;
import org.nithyakarma.qa.pages.AppHeader;
import org.nithyakarma.qa.pages.TodayPage;
import org.nithyakarma.qa.support.Session;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

// mirrors playwright-ts/tests/app/today.spec.ts
public class TodayTest extends LoggedInBaseTest {
    private static final String KALAM_TIME = "\\d{2}:\\d{2}-\\d{2}:\\d{2}";

    private TodayPage today;
    private AppHeader header;

    @BeforeMethod
    public void openToday() {
        today = new TodayPage(driver, wait);
        header = new AppHeader(driver, wait);
        today.open(baseUrl);
    }

    @Test
    public void headerShowsLogoTabsStreakPillAndLogout() {
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(header.isLogoVisible()).as("logo").isTrue();
        for (Map.Entry<String, String> tab : AppHeader.TABS.entrySet()) {
            softly.assertThat(header.tab(tab.getKey()).getDomAttribute("href")).as(tab.getKey()).isEqualTo(tab.getValue());
        }
        softly.assertThat(header.tab("Today").getDomAttribute("aria-current")).as("Today is current").isEqualTo("page");
        softly.assertThat(header.streakPillText()).as("streak pill").matches("\\d+");
        softly.assertThat(header.isLogoutVisible()).as("logout").isTrue();
        softly.assertAll();
    }

    @Test
    public void everyTabNavigatesToItsPage() {
        for (String name : AppHeader.TABS.keySet()) {
            header.goTo(name);
            assertThat(header.tab(name).getDomAttribute("aria-current")).as(name).isEqualTo("page");
        }
    }

    @Test
    public void greetingBlockShowsTodayInIstAndUserName() {
        assertThat(today.dateLine()).isEqualTo(TodayPage.expectedDateLine());
        assertThat(today.greeting()).matches("Namaskaram, \\S+.*");
        assertThat(today.progressLine()).matches("\\d+ anushtanams? done today.*");
    }

    @Test
    public void panchangamBoxShowsDayDetailsAndThreeKalams() {
        assertThat(today.panchangamText()).contains("Nakshatram", "Times shown in IST");
        Map<String, String> kalams = today.kalamTimes();
        assertThat(kalams).containsOnlyKeys("Rahu Kalam", "Yamagandam", "Gulika Kalam");
        SoftAssertions softly = new SoftAssertions();
        kalams.forEach((name, time) -> softly.assertThat(time).as(name).matches(KALAM_TIME));
        softly.assertAll();
    }

    @Test
    public void familySwitcherShowsMeAndAddChild() {
        assertThat(today.isMeVisible()).isTrue();
        assertThat(today.isAddChildVisible()).isTrue();
    }

    @Test
    public void addChildOpensProfileFamilySection() {
        today.clickAddChild();
        wait.until(ExpectedConditions.urlMatches("/profile#family$"));
    }

    @Test
    public void streakCardShowsCurrentStreakBestPunyaAndTier() {
        assertThat(today.streakCardText())
            .contains("Current Streak")
            .containsPattern("\\d+ days?")
            .containsPattern("Best: \\d+ days?")
            .containsPattern("\\d+ punya");
    }

    @Test
    public void todaysAnushtanamsListShowsSandhyaSlotsAndMarkDone() {
        assertThat(today.isAnushtanamsHeadingVisible()).isTrue();
        assertThat(today.practiceCardCount()).isGreaterThan(0);
        SoftAssertions softly = new SoftAssertions();
        for (String slot : List.of("Morning", "Noon", "Evening")) {
            softly.assertThat(today.slotButton(slot).isDisplayed()).as(slot).isTrue();
        }
        softly.assertThat(today.sandhyaProgress()).as("progress").matches("\\d of 3 sandhyas done.*");
        // UI only, Mark Done is never clicked (would add punya/streak to the test account)
        softly.assertThat(today.isMarkDoneVisible("Hanuman Chalisa")).as("Mark Done").isTrue();
        softly.assertAll();
    }

    @Test
    public void markingOneSandhyaSlotUpdatesProgress() {
        // writes punya to the account, so opt-in only (CI would mark one every day)
        if (!Session.flag("E2E_ALLOW_DATA_WRITES")) {
            throw new SkipException("set E2E_ALLOW_DATA_WRITES=1 to run (writes a log to the account)");
        }
        int before = today.sandhyasDone();
        // agreed scope: the test account only ever gets ONE slot per day
        if (before >= 1) throw new SkipException("already " + before + " of 3 done today - not marking another");
        assertThat(today.markMorningSandhya()).as("default Gayatri count").isEqualTo("108");
        assertThat(today.sandhyaProgress()).startsWith("1 of 3 sandhyas done");
        assertThat(today.slotButton("Morning").isEnabled()).isFalse();
        assertThat(today.slotButton("Morning").getDomAttribute("class")).contains("done");
    }

    // ---- Add an anushtanam ----

    @Test
    public void pickerListsOptionsAndMarksTrackedOnes() {
        today.openPicker();
        WebElement sandhya = today.pickerOption("Sandhyavandhanam");
        assertThat(sandhya.isEnabled()).isFalse();
        wait.until(d -> TodayPage.text(today.pickerOption("Sandhyavandhanam")).contains("already tracking"));
        assertThat(today.pickerOptions().size()).isGreaterThan(5);
    }

    @Test
    public void searchFiltersPicker() {
        today.openPicker();
        today.search("Gita");
        today.waitForPickerCount(1);
        assertThat(TodayPage.optionName(today.pickerOptions().get(0))).isEqualTo("Bhagavad Gita Parayanam");
    }

    @Test
    public void addingUntrackedAnushtanamShowsItsCard() {
        // CLEANUP PENDING: the app has no remove-anushtanam UI yet, so each run
        // permanently tracks one more practice. Opt-in only, so CI pushes don't pile them up.
        if (!Session.flag("E2E_ALLOW_DATA_WRITES")) {
            throw new SkipException("set E2E_ALLOW_DATA_WRITES=1 to run (adds data with no cleanup yet)");
        }
        today.openPicker();
        WebElement option = today.pickerOptions().stream().filter(WebElement::isEnabled).findFirst()
            .orElseThrow(() -> new SkipException("every anushtanam is already tracked"));
        String name = TodayPage.optionName(option);

        option.click();
        today.waitForPracticeCard(name);
        today.openPicker();
        assertThat(today.pickerOption(name).isEnabled()).isFalse();
    }
}
