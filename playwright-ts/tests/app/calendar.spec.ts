import { test, expect } from '@playwright/test';
import { CalendarPage } from '../../pages/CalendarPage';

const KALAM_TIME = /\d{2}:\d{2}-\d{2}:\d{2}/;

test.describe('Calendar (Panchangam) page', () => {
    test.beforeEach(async ({ page }) => {
        await new CalendarPage(page).open();
    });

    test('day view shows panchangam details and kalams', async ({ page }) => {
        const calendar = new CalendarPage(page);
        await expect(calendar.viewButton('Day')).toHaveAttribute('aria-pressed', 'true');
        for (const label of ['Thithi', 'Nakshatram', 'Varsham']) {
            await expect.soft(calendar.detail(label), label).toBeVisible();
        }
        await expect(calendar.kalamsHeading).toBeVisible();
        for (const name of ['Rahu Kalam', 'Yamagandam', 'Gulika Kalam']) {
            await expect.soft(calendar.kalam(name), name).toContainText(KALAM_TIME);
        }
    });

    test('Next and Previous move one day and back', async ({ page }) => {
        const calendar = new CalendarPage(page);
        // textContent matches what toHaveText compares (innerText adds line breaks)
        const start = (await calendar.periodTitle.textContent()) ?? '';

        await calendar.nextButton.click();
        await expect(calendar.periodTitle).not.toHaveText(start);

        await calendar.previousButton.click();
        await expect(calendar.periodTitle).toHaveText(start);
    });

    test('Week and Month views can be selected', async ({ page }) => {
        const calendar = new CalendarPage(page);

        await calendar.viewButton('Week').click();
        await expect(calendar.viewButton('Week')).toHaveAttribute('aria-pressed', 'true');
        // week title is a range like "27 Sept - 3 Oct"
        await expect(calendar.periodTitle).toContainText(' - ');

        await calendar.viewButton('Month').click();
        await expect(calendar.viewButton('Month')).toHaveAttribute('aria-pressed', 'true');
        await expect(calendar.viewButton('Day')).toHaveAttribute('aria-pressed', 'false');
    });
});
