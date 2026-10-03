import { test, expect } from '@playwright/test';
import { LearningPage } from '../../pages/LearningPage';

test.describe('Learning page', () => {
    test.beforeEach(async ({ page }) => {
        await new LearningPage(page).open();
    });

    test('lists every read-along practice', async ({ page }) => {
        const learning = new LearningPage(page);
        await expect(learning.practiceLinks).toHaveCount(LearningPage.PRACTICES.length);
        for (const name of LearningPage.PRACTICES) {
            await expect.soft(learning.practiceLink(name), name).toBeVisible();
        }
    });

    test('opening Hanuman Chalisa shows the reader with language switch', async ({ page }) => {
        const learning = new LearningPage(page);
        await learning.practiceLink('Hanuman Chalisa').click();
        await expect(page).toHaveURL(/\/learning\/hanuman-chalisa$/);
        await expect(page.getByRole('heading', { level: 1, name: 'Hanuman Chalisa' })).toBeVisible();
        await expect(page.getByRole('link', { name: 'Watch on YouTube' })).toBeVisible();

        await expect(learning.languageButton('English')).toHaveAttribute('aria-pressed', 'true');
        await learning.languageButton('Sanskrit').click();
        await expect(learning.languageButton('Sanskrit')).toHaveAttribute('aria-pressed', 'true');
        await expect(learning.languageButton('English')).toHaveAttribute('aria-pressed', 'false');
    });
});
