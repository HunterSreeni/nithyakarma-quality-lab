import { expect, type Page, type Locator } from '@playwright/test';

export class HistoryPage {
    readonly page: Page;
    readonly heading: Locator;
    readonly meButton: Locator;
    readonly addChildButton: Locator;
    // each history row starts with a date like "Tue, 18 Aug, 2026"
    readonly entryDates: Locator;

    constructor(page: Page) {
        this.page = page;
        this.heading = page.getByRole('heading', { level: 1, name: 'History' });
        this.meButton = page.getByRole('button', { name: /Me$/ });
        this.addChildButton = page.getByRole('button', { name: '+ Add child' });
        this.entryDates = page.locator('main').getByText(/^[A-Z][a-z]{2}, \d{2} [A-Z][a-z]{2}, \d{4}$/);
    }

    async open(): Promise<void> {
        await this.page.goto('/history');
        await expect(this.heading).toBeVisible();
    }
}
