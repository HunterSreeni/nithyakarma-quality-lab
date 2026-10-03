import { expect, type Page, type Locator } from '@playwright/test';

export class ProfilePage {
    readonly page: Page;
    readonly name: Locator;
    readonly tierProgress: Locator;
    readonly displayNameInput: Locator;
    readonly saveButton: Locator;
    readonly familySection: Locator;
    readonly addFamilyMemberButton: Locator;
    readonly childNameInput: Locator;
    readonly addChildSubmit: Locator;
    readonly inviteCode: Locator;
    readonly leaderboardOptIn: Locator;
    readonly deleteConfirmInput: Locator;
    readonly deleteAccountButton: Locator;
    readonly signOutButton: Locator;

    constructor(page: Page) {
        this.page = page;
        this.name = page.locator('main h1');
        this.tierProgress = page.getByText(/^\d+ \/ \d+ punya points/);
        this.displayNameInput = page.getByRole('textbox', { name: 'Display name' });
        this.saveButton = page.getByRole('button', { name: 'Save changes' });
        this.familySection = page.getByRole('heading', { name: /^Family Members/ }).locator('..');
        this.addFamilyMemberButton = page.getByRole('button', { name: '+ Add family member' });
        this.childNameInput = page.getByRole('textbox', { name: "Child's name" });
        this.addChildSubmit = page.getByRole('button', { name: 'Add', exact: true });
        this.inviteCode = page.getByRole('heading', { name: 'Invite & earn rewards' }).locator('..').locator('strong');
        this.leaderboardOptIn = page.getByRole('checkbox', { name: /^Show me on community leaderboards/ });
        this.deleteConfirmInput = page.getByRole('heading', { name: 'Danger zone' }).locator('..').getByRole('textbox');
        this.deleteAccountButton = page.getByRole('button', { name: 'Delete my account & all data' });
        this.signOutButton = page.getByRole('button', { name: 'Sign out' });
    }

    // the app paints its cached profile first and the server copy ~0.3s later
    static readonly CACHE_KEY = 'nk_profile_cache_v1';

    async open(): Promise<void> {
        await this.page.goto('/profile');
        await this.freshLoad();
    }

    // drop the cache, reload, and wait until the app has rewritten it from the server
    async freshLoad(): Promise<void> {
        await this.page.evaluate(key => localStorage.removeItem(key), ProfilePage.CACHE_KEY);
        await this.page.reload();
        await this.page.waitForFunction(key => localStorage.getItem(key) !== null, ProfilePage.CACHE_KEY);
        await expect(this.displayNameInput).toBeVisible();
    }

    async isChipOn(name: string): Promise<boolean> {
        return /\bon\b/.test((await this.chip(name).getAttribute('class')) ?? '');
    }

    // Streak / Best / Punya number tiles
    stat(label: 'Streak' | 'Best' | 'Punya'): Locator {
        return this.page.locator('main').getByText(label, { exact: true }).locator('..');
    }

    // radio-chip buttons (Tamil/Malayalam, Bachelor/Married); the chosen one has class "on"
    chip(name: string): Locator {
        return this.page.getByRole('button', { name, exact: true });
    }

    childRow(name: string): Locator {
        return this.familySection.locator(':scope > *')
            .filter({ hasText: name })
            .filter({ has: this.page.getByRole('button', { name: 'Remove' }) });
    }

    async addChild(name: string, gender: 'Boy' | 'Girl'): Promise<void> {
        await this.addFamilyMemberButton.click();
        await this.childNameInput.fill(name);
        await this.chip(gender).click();
        await this.addChildSubmit.click();
    }

    // Remove asks a native confirm(): "Remove <name> and all their logs?"
    async removeChild(name: string): Promise<string> {
        let message = '';
        this.page.once('dialog', async dialog => {
            message = dialog.message();
            await dialog.accept();
        });
        await this.childRow(name).getByRole('button', { name: 'Remove' }).click();
        await expect(this.familySection.getByText(name, { exact: true })).toHaveCount(0);
        return message;
    }
}
