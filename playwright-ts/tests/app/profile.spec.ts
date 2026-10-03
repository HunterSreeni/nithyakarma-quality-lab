import { test, expect } from '@playwright/test';
import { ProfilePage } from '../../pages/ProfilePage';

test.describe('Profile page', () => {
    test.beforeEach(async ({ page }) => {
        await new ProfilePage(page).open();
    });

    test('shows name, tier progress and stats', async ({ page }) => {
        const profile = new ProfilePage(page);
        await expect(profile.name).not.toBeEmpty();
        await expect(profile.tierProgress).toBeVisible();
        for (const label of ['Streak', 'Best', 'Punya'] as const) {
            await expect.soft(profile.stat(label), label).toContainText(/\d+/);
        }
        await expect(profile.inviteCode).toHaveText(/^[0-9a-f]{8}$/);
    });

    test('Save is only enabled after the display name changes', async ({ page }) => {
        const profile = new ProfilePage(page);
        const original = await profile.displayNameInput.inputValue();
        await expect(profile.saveButton).toBeDisabled();

        await profile.displayNameInput.fill(`${original}x`);
        await expect(profile.saveButton).toBeEnabled();

        // put it back, never saved
        await profile.displayNameInput.fill(original);
        await expect(profile.saveButton).toBeDisabled();
    });

    test('delete account stays disabled without the email typed', async ({ page }) => {
        const profile = new ProfilePage(page);
        await expect(profile.deleteAccountButton).toBeDisabled();
        await profile.deleteConfirmInput.fill('not-the-right@email.com');
        await expect(profile.deleteAccountButton).toBeDisabled();
    });

    test('footer links are present', async ({ page }) => {
        for (const [name, href] of [['About', '/about'], ['How Punya & Tiers Work', '/karma'], ['Terms & Conditions', '/terms'], ['Privacy Policy', '/privacy']]) {
            await expect.soft(page.getByRole('link', { name }), name).toHaveAttribute('href', href);
        }
    });
});
