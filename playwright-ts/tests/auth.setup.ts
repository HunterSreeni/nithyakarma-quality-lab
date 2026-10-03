import { test as setup, expect } from '@playwright/test';
import path from 'path';
import { loginWithMagicLink } from '../utils/session';

export const AUTH_FILE = path.join(__dirname, '../playwright/.auth/user.json');

// runs once before the logged-in tests; they all reuse this saved session
setup('authenticate as test user', async ({ page }) => {
    await loginWithMagicLink(page);
    await expect(page.getByRole('heading', { name: /^Namaskaram,/ })).toBeVisible();
    // save only the login, not the app's profile cache (stale values would paint first)
    await page.evaluate(() => localStorage.removeItem('nk_profile_cache_v1'));
    await page.context().storageState({ path: AUTH_FILE });
});
