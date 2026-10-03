import {expect, type Page, type Locator } from '@playwright/test';



export class LoginPage {
    readonly page: Page;
    readonly logo: Locator;
    readonly heroImg: Locator;
    readonly wlcmText: Locator;
    readonly ssoGoogle: Locator;
    readonly emailInput: Locator;
    readonly passwordInput: Locator;
    readonly forgotPassword: Locator;
    readonly cloudFlare: Locator;
    readonly loginButton: Locator;
    readonly signupButton: Locator;
    readonly tandcText: Locator;

    
    constructor(page: Page) {
        this.page = page;
        // Periyava image
        this.heroImg = page.getByRole('img', { name: 'Periyava' });
        // Logo 
        this.logo = page.getByRole('img', { name: 'Nithyakarma' });
        // Welcome back text
        this.wlcmText = page.getByRole('heading', { name: 'Welcome back' });
        // Google SSO
        this.ssoGoogle = page.getByRole('button', { name: 'Continue with Google' });
        // Email and password Input fields
        this.emailInput = page.getByLabel('Email');
        this.passwordInput = page.getByLabel('Password');
        // Forgot password button
        this.forgotPassword = page.getByRole('button', {name: 'Forgot password?'});
        // cloud flare robot check since it has no label/role/text resolved to CSS locator.
        this.cloudFlare = page.locator('div.turnstile-widget');
        // Login button
        // DEV NOTE - the login button does not show login when loaded,
        // instead the captcha loading state is copied into the login button,
        // saying 'Verifying...' 
        this.loginButton = page.getByRole('button', {name: 'Verifying...'});
        // Create account button
        this.signupButton = page.getByRole('button', {name: 'Create account'});
        // terms and conditions text
        this.tandcText = page.getByText('By continuing you agree to our');
    }
    async visitAndCheck(): Promise<void> {
        await this.page.goto('/');
        await expect(this.logo).toBeVisible();
    }

    async checkLoginElmns(): Promise<void> {
        await expect.soft(this.heroImg).toBeVisible();
        await expect.soft(this.wlcmText).toBeVisible();
        await expect.soft(this.ssoGoogle).toBeVisible();
        await expect.soft(this.emailInput).toBeVisible();
        await expect.soft(this.passwordInput).toBeVisible();
        await expect.soft(this.forgotPassword).toBeVisible();
        await expect.soft(this.cloudFlare).toBeVisible();
        await expect.soft(this.loginButton).toBeVisible();
        await expect.soft(this.signupButton).toBeVisible();
        await expect.soft(this.tandcText).toBeVisible();
    }

    async fieldValidation(field: Locator): Promise<string> {
        return field.evaluate((el: HTMLInputElement) => {
            const v = el.validity;
            if (v.valueMissing) return 'valueMissing';
            if (v.typeMismatch) return 'typeMismatch';
            if (v.tooShort) return 'tooShort';
            return v.valid ? 'valid' : 'other';
        });
    }

    // wait for the captcha to complete and login button to appear
    async solveCaptcha(): Promise<void> {
        const signIn = this.page.getByRole('button', {name: 'Sign In'});
        // fixed 6 seconds timeout for the cloudflare widget to load
        await this.page.waitForTimeout(6000);
        // sometimes captcha passes by itself so only click if needed.
        if (!(await signIn.isVisible())) {
            // click the checkbox if signin is not visible
            await this.cloudFlare.click();
            // give cloudflare 6 seconds more to accept the click
            await  this.page.waitForTimeout(6000);
        }
        // now check if signin button is enabled and click
        // await expect(signIn).toBeEnabled({ timeout: 10000});
        // negative testing of the captcha failing for bots
        await expect(this.loginButton).toBeVisible();
    }

    async fillCredentials(email: string, password: string): Promise<void> {
        await this.emailInput.fill(email);
        await this.passwordInput.fill(password);
    }
    async submit(): Promise<void> {
        await this.page.getByRole('button', {name: 'Sign In'}).click();
    }
}