import { expect, type Page, type Locator } from '@playwright/test';

export class TodayPage {
    readonly page: Page;
    readonly dateLine: Locator;
    readonly greeting: Locator;
    readonly progressLine: Locator;
    readonly panchangamBox: Locator;
    readonly kalams: Locator;
    readonly memberSwitcher: Locator;
    readonly addChildButton: Locator;
    readonly streakCard: Locator;
    readonly anushtanamsHeading: Locator;
    readonly practiceCards: Locator;
    readonly sandhyaProgress: Locator;
    readonly addAnushtanamToggle: Locator;
    readonly pickerSearch: Locator;

    constructor(page: Page) {
        this.page = page;
        // "Saturday, 03 October 2026"
        this.dateLine = page.locator('main .eyebrow');
        this.greeting = page.getByRole('heading', { level: 1, name: /^Namaskaram,/ });
        this.progressLine = page.locator('main .greet-sub');
        // panchangam + kalams have no roles, the app's class names are the stable handle
        this.panchangamBox = page.locator('.panchangam-box');
        this.kalams = page.locator('.pb-kalam');
        this.memberSwitcher = page.getByRole('button', { name: /Me$/ }).locator('..');
        this.addChildButton = page.getByRole('button', { name: '+ Add child' });
        this.streakCard = page.locator('main').getByText('Current Streak').locator('../..');
        this.anushtanamsHeading = page.getByRole('heading', { name: "Today's Anushtanams" });
        this.practiceCards = page.locator('.practice-card');
        this.sandhyaProgress = page.getByText(/\d of 3 sandhyas done/);
        this.addAnushtanamToggle = page.getByRole('button', { name: /^Add an anushtanam to track/ });
        this.pickerSearch = page.getByRole('textbox', { name: 'Search...' });
    }

    async open(): Promise<void> {
        await this.page.goto('/');
        await expect(this.greeting).toBeVisible();
    }

    // "Saturday, 03 October 2026" for today in IST
    static expectedDateLine(now = new Date()): string {
        const part = (opts: Intl.DateTimeFormatOptions) =>
            new Intl.DateTimeFormat('en-GB', { timeZone: 'Asia/Kolkata', ...opts }).format(now);
        return `${part({ weekday: 'long' })}, ${part({ day: '2-digit' })} ${part({ month: 'long' })} ${part({ year: 'numeric' })}`;
    }

    practiceCard(name: string): Locator {
        return this.practiceCards.filter({ has: this.page.locator('.p-name', { hasText: name }) });
    }

    slotButton(slot: 'Morning' | 'Noon' | 'Evening'): Locator {
        return this.page.getByRole('button', { name: slot, exact: true });
    }

    async sandhyasDone(): Promise<number> {
        // textContent, not innerText: CSS uppercases it on screen ("0 OF 3 SANDHYAS DONE")
        const text = (await this.sandhyaProgress.textContent()) ?? '';
        return Number(text.match(/(\d) of 3/)![1]);
    }

    async openPicker(): Promise<void> {
        await this.addAnushtanamToggle.click();
        await expect(this.pickerSearch).toBeVisible();
        await expect(this.pickerOptions().first()).toBeVisible();
    }

    // picker = .dropdown under the toggle; options are buttons (a muted "Loading..." div shows first)
    pickerOption(name: string): Locator {
        return this.pickerOptions().filter({ hasText: new RegExp(`^${name}`) });
    }

    pickerOptions(): Locator {
        return this.page.locator('.dropdown').getByRole('button');
    }
}
