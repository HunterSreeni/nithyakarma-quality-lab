package org.nithyakarma.qa.tests;

import org.assertj.core.api.SoftAssertions;
import org.nithyakarma.qa.base.LoggedInBaseTest;
import org.nithyakarma.qa.pages.CalendarPage;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// mirrors playwright-ts/tests/app/calendar.spec.ts
public class CalendarTest extends LoggedInBaseTest {
    private CalendarPage calendar;

    @BeforeMethod
    public void openCalendar() {
        calendar = new CalendarPage(driver, wait);
        calendar.open(baseUrl);
    }

    @Test
    public void dayViewShowsPanchangamDetailsAndKalams() {
        assertThat(calendar.isViewPressed("Day")).isTrue();
        SoftAssertions softly = new SoftAssertions();
        for (String label : List.of("Thithi", "Nakshatram", "Varsham")) {
            softly.assertThat(calendar.isDetailVisible(label)).as(label).isTrue();
        }
        softly.assertThat(calendar.isKalamsHeadingVisible()).as("kalams heading").isTrue();
        for (String name : List.of("Rahu Kalam", "Yamagandam", "Gulika Kalam")) {
            softly.assertThat(calendar.kalamText(name)).as(name).containsPattern("\\d{2}:\\d{2}-\\d{2}:\\d{2}");
        }
        softly.assertAll();
    }

    @Test
    public void nextAndPreviousMoveOneDayAndBack() {
        String start = calendar.periodTitle();
        calendar.next();
        calendar.waitForTitleNot(start);
        calendar.previous();
        calendar.waitForTitle(start);
    }

    @Test
    public void weekAndMonthViewsCanBeSelected() {
        calendar.selectView("Week");
        // week title is a range like "27 Sept - 3 Oct"
        assertThat(calendar.periodTitle()).contains(" - ");

        calendar.selectView("Month");
        assertThat(calendar.isViewPressed("Day")).isFalse();
    }
}
