import { test, expect } from '@playwright/test';
import { HistoryPage } from '../../pages/HistoryPage';

test.describe('History page', () => {
    test('shows the family switcher and dated entries', async ({ page }) => {
        const history = new HistoryPage(page);
        await history.open();
        await expect(history.meButton).toBeVisible();
        await expect(history.addChildButton).toBeVisible();
        // the test account has past logs, so at least one dated row
        await expect(history.entryDates.first()).toBeVisible();
    });
});
