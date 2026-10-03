import { defineConfig, devices } from '@playwright/test';
import path from 'path';

import fs from 'fs';

// local runs read the root .env; CI passes the same values as secrets
const envFile = path.resolve(__dirname, '../.env');
if (fs.existsSync(envFile)) process.loadEnvFile(envFile);

/**
 * Read environment variables from file.
 * https://github.com/motdotla/dotenv
 */
// import dotenv from 'dotenv';
// import path from 'path';
// dotenv.config({ path: path.resolve(__dirname, '.env') });

/**
 * See https://playwright.dev/docs/test-configuration.
 */
export default defineConfig({
  testDir: './tests',
  /* Run tests in files in parallel */
  fullyParallel: true,
  /* Fail the build on CI if you accidentally left test.only in the source code. */
  forbidOnly: !!process.env.CI,
  /* Retry on CI only */
  retries: process.env.CI ? 2 : 0,
  /* Opt out of parallel tests on CI. */
  workers: process.env.CI ? 1 : undefined,
  /* Reporter to use. See https://playwright.dev/docs/test-reporters */
  // html for humans, json feeds the results dashboard
  reporter: [
    ['list'],
    ['html', { open: 'never' }],
    ['json', { outputFile: '../results/playwright.json' }],
  ],
  /* Shared settings for all the projects below. See https://playwright.dev/docs/api/class-testoptions. */
  use: {
    /* Base URL to use in actions like `await page.goto('')`. */
    baseURL: process.env.BASE_URL,
    // the app shows IST dates/kalams; pin it so CI (UTC) matches
    timezoneId: 'Asia/Kolkata',

    /* Collect trace when retrying the failed test. See https://playwright.dev/docs/trace-viewer */
    trace: 'on-first-retry',
  },

  /* Configure projects for major browsers */
  projects: [
    // logged-out tests: login page, form validation, captcha + API negatives
    {
      name: 'chromium',
      testIgnore: ['**/app/**', '**/*.setup.ts'],
      use: { ...devices['Desktop Chrome'],
        launchOptions: {
          args: ['--disable-blink-features=AutomationControlled'],
          ignoreDefaultArgs: ['--enable-automation']
        },
       },
    },
    // magic-link login once, saved to playwright/.auth/user.json
    {
      name: 'setup',
      testMatch: /auth\.setup\.ts/,
    },
    // logged-in app tests reuse the saved session
    {
      name: 'app',
      testMatch: '**/app/**/*.spec.ts',
      testIgnore: ['**/logout.spec.ts', '**/settings.spec.ts'],
      dependencies: ['setup'],
      use: { ...devices['Desktop Chrome'], storageState: 'playwright/.auth/user.json' },
    },
    // settings round-trips (switch + switch back) run after the read-only checks
    {
      name: 'settings',
      testMatch: '**/app/settings.spec.ts',
      dependencies: ['app'],
      use: { ...devices['Desktop Chrome'], storageState: 'playwright/.auth/user.json' },
    },
    // logout runs last so it can't end the session the other tests are using
    {
      name: 'logout',
      testMatch: '**/app/logout.spec.ts',
      dependencies: ['settings'],
      use: { ...devices['Desktop Chrome'] },
    },

    // {
    //   name: 'firefox',
    //   use: { ...devices['Desktop Firefox'] },
    // },

    // {
    //   name: 'webkit',
    //   use: { ...devices['Desktop Safari'] },
    // },

    /* Test against mobile viewports. */
    // {
    //   name: 'Mobile Chrome',
    //   use: { ...devices['Pixel 5'] },
    // },
    // {
    //   name: 'Mobile Safari',
    //   use: { ...devices['iPhone 12'] },
    // },

    /* Test against branded browsers. */
    // {
    //   name: 'Microsoft Edge',
    //   use: { ...devices['Desktop Edge'], channel: 'msedge' },
    // },
    // {
    //   name: 'Google Chrome',
    //   use: { ...devices['Desktop Chrome'], channel: 'chrome' },
    // },
  ],

  /* Run your local dev server before starting the tests */
  // webServer: {
  //   command: 'npm run start',
  //   url: 'http://localhost:3000',
  //   reuseExistingServer: !process.env.CI,
  // },

});
