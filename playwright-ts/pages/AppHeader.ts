import { type Page, type Locator } from '@playwright/test';

// top bar shared by every logged-in page
export class AppHeader {
    readonly page: Page;
    readonly logo: Locator;
    readonly nav: Locator;
    readonly streakPill: Locator;
    readonly avatar: Locator;
    readonly logoutButton: Locator;

    // tab name -> url path
    static readonly TABS: Record<string, string> = {
        Today: '/',
        Learning: '/learning',
        History: '/history',
        Sabha: '/sabha',
        Calendar: '/calendar',
        Profile: '/profile',
    };

    constructor(page: Page) {
        this.page = page;
        this.logo = page.getByRole('banner').getByRole('img', { name: 'Nithyakarma' });
        this.nav = page.getByRole('navigation', { name: 'Primary' });
        // flame + number, no label/role so CSS is the only handle
        this.streakPill = page.locator('.streak-pill');
        this.avatar = page.getByRole('banner').locator('a.top-avatar');
        this.logoutButton = page.getByRole('button', { name: 'Logout' });
    }

    tab(name: string): Locator {
        return this.nav.getByRole('link', { name, exact: true });
    }

    async goTo(name: string): Promise<void> {
        await this.tab(name).click();
    }
}
