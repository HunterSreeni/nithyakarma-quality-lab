package org.nithyakarma.qa.tests;

import org.assertj.core.api.SoftAssertions;
import org.nithyakarma.qa.base.LoggedInBaseTest;
import org.nithyakarma.qa.pages.ProfilePage;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

// mirrors playwright-ts/tests/app/profile.spec.ts
public class ProfileTest extends LoggedInBaseTest {
    private ProfilePage profile;

    @BeforeMethod
    public void openProfile() {
        profile = new ProfilePage(driver, wait);
        profile.open(baseUrl);
    }

    @Test
    public void showsNameTierProgressAndStats() {
        assertThat(profile.name()).isNotBlank();
        assertThat(profile.tierProgress()).matches("\\d+ / \\d+ punya points.*");
        SoftAssertions softly = new SoftAssertions();
        for (String label : List.of("Streak", "Best", "Punya")) {
            softly.assertThat(profile.stat(label)).as(label).containsPattern("\\d+");
        }
        softly.assertAll();
        assertThat(profile.inviteCode()).matches("[0-9a-f]{8}");
    }

    @Test
    public void saveIsOnlyEnabledAfterDisplayNameChanges() {
        String original = profile.displayName();
        assertThat(profile.isSaveEnabled()).isFalse();

        profile.typeDisplayName(original + "x");
        profile.waitForSaveEnabled(true);

        // put it back, never saved
        profile.typeDisplayName(original);
        profile.waitForSaveEnabled(false);
    }

    @Test
    public void deleteAccountStaysDisabledWithoutEmailTyped() {
        assertThat(profile.isDeleteEnabled()).isFalse();
        profile.typeDeleteConfirmation("not-the-right@email.com");
        assertThat(profile.isDeleteEnabled()).isFalse();
    }

    @Test
    public void footerLinksArePresent() {
        Map<String, String> links = new LinkedHashMap<>();
        links.put("About", "/about");
        links.put("How Punya & Tiers Work", "/karma");
        links.put("Terms & Conditions", "/terms");
        links.put("Privacy Policy", "/privacy");
        SoftAssertions softly = new SoftAssertions();
        links.forEach((text, href) -> softly.assertThat(profile.footerLinkHref(text)).as(text).isEqualTo(href));
        softly.assertAll();
    }
}
