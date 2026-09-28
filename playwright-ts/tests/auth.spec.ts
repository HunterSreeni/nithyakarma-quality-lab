import { test, expect } from '@playwright/test';
import { LoginPage } from '../pages/LoginPage';


test.describe('Auth Page', () => {
    test('Should navigate to the Login Page', async ({page}) => {
        // Page UI check
        const loginPage = new LoginPage(page);
        await loginPage.visitAndCheck();
        await loginPage.checkLoginElmns();
    });
});