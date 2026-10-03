import { expect, type Page, type Locator } from '@playwright/test';

export class CalendarPage {
    readonly page: Page;
    readonly heading: Locator;
    readonly periodTitle: Locator;
    readonly previousButton: Locator;
    readonly nextButton: Locator;
    readonly viewGroup: Locator;
    readonly kalamsHeading: Locator;

    constructor(page: Page) {
        this.page = page;
        this.heading = page.getByRole('heading', { level: 1, name: 'Panchangam' });
        // the block holding "புரட்டாசி 17 / Purattasi 17" next to Previous/Next
        this.previousButton = page.getByRole('button', { name: 'Previous' });
        this.nextButton = page.getByRole('button', { name: 'Next' });
        this.periodTitle = this.previousButton.locator('../..').locator(':scope > :first-child');
        this.viewGroup = page.getByRole('group', { name: 'Calendar view' });
        this.kalamsHeading = page.getByRole('heading', { level: 2, name: 'Kalams to avoid' });
    }

    async open(): Promise<void> {
        await this.page.goto('/calendar');
        await expect(this.heading).toBeVisible();
    }

    viewButton(name: 'Day' | 'Week' | 'Month'): Locator {
        return this.viewGroup.getByRole('button', { name, exact: true });
    }

    // label -> value rows inside the Panchangam section (Thithi, Nakshatram, Varsham)
    detail(label: string): Locator {
        return this.page.locator('main').getByText(label, { exact: true });
    }

    kalam(name: string): Locator {
        return this.page.locator('main').getByText(name, { exact: true }).locator('..');
    }
}
