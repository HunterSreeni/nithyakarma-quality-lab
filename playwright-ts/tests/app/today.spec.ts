import { test, expect } from '@playwright/test';
import { AppHeader } from '../../pages/AppHeader';
import { TodayPage } from '../../pages/TodayPage';

const KALAM_TIME = /^\d{2}:\d{2}-\d{2}:\d{2}$/;

test.describe('Today page', () => {
    test.beforeEach(async ({ page }) => {
        await new TodayPage(page).open();
    });

    test('header shows logo, all tabs, streak pill and logout', async ({ page }) => {
        const header = new AppHeader(page);
        await expect(header.logo).toBeVisible();
        for (const [name, path] of Object.entries(AppHeader.TABS)) {
            await expect.soft(header.tab(name), name).toHaveAttribute('href', path);
        }
        await expect(header.tab('Today')).toHaveAttribute('aria-current', 'page');
        await expect(header.streakPill).toHaveText(/^\s*\d+\s*$/);
        await expect(header.logoutButton).toBeVisible();
    });

    test('every tab navigates to its page', async ({ page }) => {
        const header = new AppHeader(page);
        for (const [name, path] of Object.entries(AppHeader.TABS)) {
            await header.goTo(name);
            await expect(page, name).toHaveURL(path === '/' ? /\/$/ : new RegExp(`${path}$`));
            await expect(header.tab(name)).toHaveAttribute('aria-current', 'page');
        }
    });

    test('greeting block shows today in IST and the user name', async ({ page }) => {
        const today = new TodayPage(page);
        await expect(today.dateLine).toHaveText(TodayPage.expectedDateLine());
        await expect(today.greeting).toHaveText(/^Namaskaram, \S+/);
        await expect(today.progressLine).toHaveText(/^\d+ anushtanams? done today/);
    });

    test('panchangam box shows the day details and three kalams', async ({ page }) => {
        const today = new TodayPage(page);
        await expect(today.panchangamBox).toBeVisible();
        await expect(today.panchangamBox).toContainText('Nakshatram');
        await expect(today.panchangamBox).toContainText('Times shown in IST');
        await expect(today.kalams).toHaveCount(3);
        for (const name of ['Rahu Kalam', 'Yamagandam', 'Gulika Kalam']) {
            const kalam = today.kalams.filter({ hasText: name });
            await expect.soft(kalam.locator('.pb-kalam-time'), name).toHaveText(KALAM_TIME);
        }
    });

    test('family switcher shows Me and Add child', async ({ page }) => {
        const today = new TodayPage(page);
        await expect(page.getByRole('button', { name: /Me$/ })).toBeVisible();
        await expect(today.addChildButton).toBeVisible();
    });

    test('Add child opens the profile family section', async ({ page }) => {
        await new TodayPage(page).addChildButton.click();
        await expect(page).toHaveURL(/\/profile#family$/);
        await expect(page.getByRole('heading', { name: /^Family Members/ })).toBeVisible();
    });

    test('streak card shows current streak, best, punya and tier', async ({ page }) => {
        const card = new TodayPage(page).streakCard;
        await expect(card).toContainText('Current Streak');
        await expect(card).toContainText(/\d+ days?/);
        await expect(card).toContainText(/Best: \d+ days?/);
        await expect(card).toContainText(/\d+ punya/);
    });

    test("today's anushtanams list shows sandhyavandhanam slots and Mark Done", async ({ page }) => {
        const today = new TodayPage(page);
        await expect(today.anushtanamsHeading).toBeVisible();
        await expect(today.practiceCards.first()).toBeVisible();
        for (const slot of ['Morning', 'Noon', 'Evening'] as const) {
            await expect.soft(today.slotButton(slot)).toBeVisible();
        }
        await expect(today.sandhyaProgress).toBeVisible();
        // UI only, Mark Done is never clicked (would add punya/streak to the test account)
        await expect(today.practiceCard('Hanuman Chalisa').getByRole('button', { name: 'Mark Done' })).toBeVisible();
    });

    test('marking one sandhya slot updates the progress (1 of 3 only)', async ({ page }) => {
        const today = new TodayPage(page);
        // writes punya to the account, so opt-in only (CI would mark one every day)
        test.skip(process.env.E2E_ALLOW_DATA_WRITES !== '1', 'set E2E_ALLOW_DATA_WRITES=1 to run (writes a log to the account)');
        const before = await today.sandhyasDone();
        // agreed scope: the test account only ever gets ONE slot per day
        test.skip(before >= 1, `already ${before} of 3 done today - not marking another`);
        await today.slotButton('Morning').click();
        // Morning asks for the Gayatri japam count first (defaults to 108)
        const countDialog = page.getByRole('dialog', { name: 'Prathakala Gayatri Count' });
        await expect(countDialog).toBeVisible();
        await expect(countDialog.getByRole('spinbutton')).toHaveValue('108');
        await countDialog.getByRole('button', { name: 'Save' }).click();

        await expect(today.sandhyaProgress).toHaveText(/^1 of 3 sandhyas done/);
        await expect(today.slotButton('Morning')).toBeDisabled();
        await expect(today.slotButton('Morning')).toHaveClass(/\bdone\b/);
    });
});

test.describe('Add an anushtanam', () => {
    test.beforeEach(async ({ page }) => {
        await new TodayPage(page).open();
    });

    test('picker lists options and marks tracked ones', async ({ page }) => {
        const today = new TodayPage(page);
        await today.openPicker();
        await expect(today.pickerOption('Sandhyavandhanam')).toBeDisabled();
        await expect(today.pickerOption('Sandhyavandhanam')).toContainText('already tracking');
        expect(await today.pickerOptions().count()).toBeGreaterThan(5);
    });

    test('search filters the picker', async ({ page }) => {
        const today = new TodayPage(page);
        await today.openPicker();
        await today.pickerSearch.fill('Gita');
        await expect(today.pickerOptions()).toHaveCount(1);
        await expect(today.pickerOptions().first()).toContainText('Bhagavad Gita Parayanam');
    });

    test('adding an untracked anushtanam shows its card', async ({ page }) => {
        // CLEANUP PENDING: the app has no remove-anushtanam UI yet, so each run
        // permanently tracks one more practice on the test account.
        test.info().annotations.push({ type: 'cleanup', description: 'pending app remove-anushtanam feature' });
        // opt-in only, so CI pushes don't keep piling up tracked practices
        test.skip(process.env.E2E_ALLOW_DATA_WRITES !== '1', 'set E2E_ALLOW_DATA_WRITES=1 to run (adds data with no cleanup yet)');
        const today = new TodayPage(page);
        await today.openPicker();

        const available = today.pickerOptions().filter({ hasNotText: 'already tracking' });
        test.skip(await available.count() === 0, 'every anushtanam is already tracked');
        const option = available.first();
        const name = (await option.innerText()).split('\n')[0].trim();

        // wait for the insert itself, so a server error fails here with its status
        const [insert] = await Promise.all([
            page.waitForResponse(r => r.url().includes('/rest/v1/user_practices') && r.request().method() === 'POST'),
            option.click(),
        ]);
        expect(insert.status(), await insert.text()).toBe(201);
        await expect(today.practiceCard(name)).toBeVisible();
        await today.openPicker();
        await expect(today.pickerOption(name)).toContainText('already tracking');
    });
});
