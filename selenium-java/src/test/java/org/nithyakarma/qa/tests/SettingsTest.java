package org.nithyakarma.qa.tests;

import org.nithyakarma.qa.base.LoggedInBaseTest;
import org.nithyakarma.qa.pages.ProfilePage;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import java.util.function.BooleanSupplier;

import static org.assertj.core.api.Assertions.assertThat;

// mirrors playwright-ts/tests/app/settings.spec.ts
// these change account state and put it back; testng.xml runs them after the read-only tests
public class SettingsTest extends LoggedInBaseTest {
    private ProfilePage profile;

    @BeforeMethod
    public void openProfile() {
        profile = new ProfilePage(driver, wait);
        profile.open(baseUrl);
    }

    @Test
    public void addChildAndRemoveIt() {
        String childName = "E2E Temp " + String.valueOf(System.currentTimeMillis()).substring(8);

        profile.addChild(childName, "Girl");
        assertThat(profile.childRowText(childName)).contains("Female");

        String confirmText = profile.removeChild(childName);
        assertThat(confirmText).isEqualTo("Remove " + childName + " and all their logs?");
        assertThat(profile.hasChild(childName)).isFalse();
    }

    @Test
    public void panchangamTraditionSwitchesAndSwitchesBack() {
        roundTrip("Tamil", "Malayalam");
    }

    @Test
    public void maritalStatusSwitchesAndSwitchesBack() {
        roundTrip("Bachelor", "Married");
    }

    @Test
    public void leaderboardOptInTogglesAndTogglesBack() {
        boolean original = profile.isLeaderboardOptedIn();

        for (boolean target : new boolean[] { !original, original }) {
            profile.setLeaderboardOptIn(target);
            waitUntilSaved(() -> profile.isLeaderboardOptedIn() == target);
        }
        assertThat(profile.isLeaderboardOptedIn()).isEqualTo(original);
    }

    // switch to the other chip, confirm it saved, switch back, confirm that saved
    private void roundTrip(String chipA, String chipB) {
        String from = profile.isChipOn(chipA) ? chipA : chipB;
        String to = from.equals(chipA) ? chipB : chipA;

        for (String target : new String[] { to, from }) {
            profile.selectChip(target);
            waitUntilSaved(() -> profile.isChipOn(target));
        }
        assertThat(profile.isChipOn(from)).as(from + " still selected").isTrue();
    }

    // a step only counts once a fresh reload (no profile cache) shows it:
    // clicking twice quickly can let the first save land last
    private void waitUntilSaved(BooleanSupplier saved) {
        wait.until(d -> {
            profile.freshLoad();
            return saved.getAsBoolean();
        });
    }
}
