import { expect, type Page, type Locator } from '@playwright/test';

export class LearningPage {
    readonly page: Page;
    readonly heading: Locator;
    readonly practiceLinks: Locator;
    // detail page (/learning/<slug>)
    readonly languageGroup: Locator;

    static readonly PRACTICES = [
        'Hanuman Chalisa', 'Vishnu Sahasranamam', 'Sai Baba Aarti', 'Lalitha Sahasranamam',
        'Soundarya Lahari', 'Ramayanam', 'Devi Mahatmyam', 'Dakshinamurthy Stotram',
        'Aditya Hrudayam', 'Subrahmanya Bhujangam', 'Mukundamala', 'Sri Rudram',
        'Sandhyavandhanam', 'Samidhadhanam',
    ];

    constructor(page: Page) {
        this.page = page;
        this.heading = page.getByRole('heading', { level: 1, name: 'Read along' });
        this.practiceLinks = page.locator('main a[href^="/learning/"]');
        this.languageGroup = page.getByRole('group', { name: 'Language' });
    }

    async open(): Promise<void> {
        await this.page.goto('/learning');
        await expect(this.heading).toBeVisible();
    }

    practiceLink(name: string): Locator {
        return this.page.getByRole('link', { name: new RegExp(`^${name}\\b`) });
    }

    languageButton(name: string): Locator {
        return this.languageGroup.getByRole('button', { name, exact: true });
    }
}
