package org.nithyakarma.qa.tests;

import org.nithyakarma.qa.base.LoggedInBaseTest;
import org.nithyakarma.qa.pages.SabhaPage;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

// mirrors playwright-ts/tests/app/sabha.spec.ts
public class SabhaTest extends LoggedInBaseTest {
    private SabhaPage sabha;

    @BeforeMethod
    public void openSabha() {
        sabha = new SabhaPage(driver, wait);
        sabha.open(baseUrl);
    }

    @Test
    public void defaultsToThisWeekAndShowsMyRow() {
        assertThat(sabha.periodLine()).contains("This week");
        assertThat(sabha.isSegmentOn("Week")).isTrue();
        assertThat(sabha.isOwnRowVisible()).isTrue();
    }

    @Test
    public void weekMonthKidsSwitchTheBoard() {
        sabha.select("Month");
        sabha.waitForPeriod("This month.*");

        sabha.select("Kids");
        sabha.waitForHeading("Bala Sabha");

        sabha.select("Week");
        sabha.waitForHeading("Sabha Leaderboard");
        assertThat(sabha.periodLine()).contains("This week");
    }
}
