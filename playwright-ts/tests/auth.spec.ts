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

test.describe('Login form validation', () => {
    const cases = [
        { name: 'bad email format',        field: 'email',    value: 'notanemail',    expected: 'typeMismatch' },
        { name: 'email missing domain',    field: 'email',    value: 'user@',         expected: 'typeMismatch' },
        { name: 'password 7 chars',        field: 'password', value: '1234567',       expected: 'tooShort' },
        { name: 'valid email',             field: 'email',    value: 'a@b.co',        expected: 'valid' },
        { name: 'password exactly 8',      field: 'password', value: '12345678',      expected: 'valid' },
        { name: 'empty email',             field: 'email',    value: '',              expected: 'valueMissing' },
        { name: 'empty password',          field: 'password', value: '',              expected: 'valueMissing' },
    ];

    for (const c of cases) {
        test(`validation: ${c.name}`, async ({ page }) => {
        // open, fill c.value into the right field, then:
        const loginPage = new LoginPage(page);
        await loginPage.visitAndCheck();

        const field = c.field === 'email' ? loginPage.emailInput : loginPage.passwordInput;
        await field.fill(c.value);
        expect(await loginPage.fieldValidation(field)).toBe(c.expected);
        });
    }
    test('log in with valid credentials', async ({ page }) => {
        test.setTimeout(60000);
        const email = process.env.E2E_EMAIL;
        const password = process.env.E2E_PASSWORD;
        if (!email || !password) throw new Error('E2E_EMAIL / E2E_PASSWORD missing from .env');
        const loginPage = new LoginPage(page);
        await loginPage.visitAndCheck();
        await loginPage.fillCredentials(email, password);
        await loginPage.solveCaptcha();
        await loginPage.submit();
        expect(loginPage.wlcmText).toBeHidden({ timeout: 15000 });

    })
})
