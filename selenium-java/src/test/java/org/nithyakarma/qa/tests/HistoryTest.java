package org.nithyakarma.qa.tests;

import org.nithyakarma.qa.base.LoggedInBaseTest;
import org.nithyakarma.qa.pages.HistoryPage;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

// mirrors playwright-ts/tests/app/history.spec.ts
public class HistoryTest extends LoggedInBaseTest {

    @Test
    public void showsFamilySwitcherAndDatedEntries() {
        HistoryPage history = new HistoryPage(driver, wait);
        history.open(baseUrl);
        assertThat(history.isMeVisible()).isTrue();
        assertThat(history.isAddChildVisible()).isTrue();
        // the test account has past logs, so at least one dated row
        assertThat(history.entryDates()).isNotEmpty();
    }
}
