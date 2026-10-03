import { expect, type Page, type Locator } from '@playwright/test';

export class SabhaPage {
    readonly page: Page;
    readonly heading: Locator;
    readonly periodLine: Locator;
    readonly ownRow: Locator;

    constructor(page: Page) {
        this.page = page;
        this.heading = page.locator('main h1');
        // "This week · resets Sunday night" / "This month"
        this.periodLine = page.locator('main h1 + *');
        this.ownRow = page.locator('main').getByText(/\(You\)$/).first();
    }

    async open(): Promise<void> {
        await this.page.goto('/sabha');
        await expect(this.heading).toHaveText('Sabha Leaderboard');
    }

    // Week / Month / Kids segmented buttons; the active one gets class "on"
    segment(name: 'Week' | 'Month' | 'Kids'): Locator {
        return this.page.getByRole('button', { name, exact: true });
    }
}
