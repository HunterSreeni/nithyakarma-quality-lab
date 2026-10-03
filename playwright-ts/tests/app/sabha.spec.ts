import { test, expect } from '@playwright/test';
import { SabhaPage } from '../../pages/SabhaPage';

test.describe('Sabha leaderboard', () => {
    test.beforeEach(async ({ page }) => {
        await new SabhaPage(page).open();
    });

    test('defaults to this week and shows my row', async ({ page }) => {
        const sabha = new SabhaPage(page);
        await expect(sabha.periodLine).toContainText('This week');
        await expect(sabha.segment('Week')).toHaveClass(/\bon\b/);
        await expect(sabha.ownRow).toBeVisible();
    });

    test('Week / Month / Kids switch the board', async ({ page }) => {
        const sabha = new SabhaPage(page);

        await sabha.segment('Month').click();
        await expect(sabha.segment('Month')).toHaveClass(/\bon\b/);
        await expect(sabha.periodLine).toContainText('This month');

        await sabha.segment('Kids').click();
        await expect(sabha.heading).toHaveText('Bala Sabha');

        await sabha.segment('Week').click();
        await expect(sabha.heading).toHaveText('Sabha Leaderboard');
        await expect(sabha.periodLine).toContainText('This week');
    });
});
