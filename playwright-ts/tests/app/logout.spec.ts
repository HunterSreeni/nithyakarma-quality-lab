import { test, expect } from '@playwright/test';
import { AppHeader } from '../../pages/AppHeader';
import { LoginPage } from '../../pages/LoginPage';
import { loginWithMagicLink } from '../../utils/session';

// own fresh session (not the shared one) and its own project that runs last,
// because logging out can revoke the user's other sessions too
test.describe('Logout', () => {
    test('Logout returns to the login page', async ({ page }) => {
        await loginWithMagicLink(page);
        const header = new AppHeader(page);
        await expect(header.logoutButton).toBeVisible();

        await header.logoutButton.click();
        await expect(new LoginPage(page).wlcmText).toBeVisible();
        await expect(header.logoutButton).toHaveCount(0);
    });
});
