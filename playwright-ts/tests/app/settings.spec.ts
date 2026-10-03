import { test, expect } from '@playwright/test';
import { ProfilePage } from '../../pages/ProfilePage';

// these change account state and put it back, so they run one after another.
// Each step only counts once a fresh reload (no profile cache) shows it saved:
// clicking twice quickly can let the first save land last.
test.describe('Profile settings round-trips', () => {
    test.describe.configure({ mode: 'serial' });

    test.beforeEach(async ({ page }) => {
        await new ProfilePage(page).open();
    });

    test('add a child and remove it', async ({ page }) => {
        const profile = new ProfilePage(page);
        const childName = `E2E Temp ${Date.now().toString().slice(-5)}`;

        await profile.addChild(childName, 'Girl');
        await expect(profile.childRow(childName)).toBeVisible();
        await expect(profile.childRow(childName)).toContainText('Female');

        const confirmText = await profile.removeChild(childName);
        expect(confirmText).toBe(`Remove ${childName} and all their logs?`);
    });

    // switch to the other chip, confirm it saved, switch back, confirm that saved
    async function chipRoundTrip(profile: ProfilePage, a: string, b: string) {
        const [from, to] = await profile.isChipOn(a) ? [a, b] : [b, a];
        for (const target of [to, from]) {
            await profile.chip(target).click();
            await expect(profile.chip(target)).toHaveClass(/\bon\b/);
            await expect.poll(async () => {
                await profile.freshLoad();
                return profile.isChipOn(target);
            }, { message: `${target} saved` }).toBe(true);
        }
    }

    test('panchangam tradition switches and switches back', async ({ page }) => {
        await chipRoundTrip(new ProfilePage(page), 'Tamil', 'Malayalam');
    });

    test('marital status switches and switches back', async ({ page }) => {
        await chipRoundTrip(new ProfilePage(page), 'Bachelor', 'Married');
    });

    test('leaderboard opt-in toggles and toggles back', async ({ page }) => {
        const profile = new ProfilePage(page);
        const original = await profile.leaderboardOptIn.isChecked();

        for (const target of [!original, original]) {
            await profile.leaderboardOptIn.setChecked(target);
            await expect.poll(async () => {
                await profile.freshLoad();
                return profile.leaderboardOptIn.isChecked();
            }, { message: `opt-in ${target} saved` }).toBe(target);
        }
    });
});
