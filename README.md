# Nithyakarma Quality Lab

[![Tests](https://github.com/HunterSreeni/nithyakarma-quality-lab/actions/workflows/tests.yml/badge.svg)](https://github.com/HunterSreeni/nithyakarma-quality-lab/actions/workflows/tests.yml)

End-to-end tests for [Nithyakarma](https://nithyakarma.org), a daily anushtanam tracker I built and run in production.
The same 39 test cases are written twice, once in **Playwright (TypeScript)** and once in **Selenium (Java + TestNG)**,
so the two tools can be compared on real work rather than toy examples.

**Live results:** [huntersreeni.github.io/nithyakarma-quality-lab](https://huntersreeni.github.io/nithyakarma-quality-lab/).
Every push runs both suites in GitHub Actions and publishes a side-by-side page with each test case's result in each framework.

## What's covered

| Area | Test cases |
|---|---|
| Login page | Page renders, 7 data-driven form validation cases |
| Security (negative) | Captcha blocks an automated browser; the auth API rejects a login with no captcha token |
| Today | Header and navigation, IST date and greeting, panchangam and kalams, streak card, anushtanam list, sandhya slots, the add-anushtanam picker and search |
| Learning, History, Sabha, Calendar | Page content, toggles, Previous/Next, Week/Month/Kids views |
| Profile | Stats, tier progress, Save enabled only after edits, delete-account guard, footer links |
| Settings round-trips | Add and remove a child, switch tradition and marital status and switch back, leaderboard opt-in on and off |
| Logout | Returns to the login page |

The full list with IDs (TC-01 to TC-39) lives in [`dashboard/test-cases.json`](dashboard/test-cases.json). It maps each case to its
Playwright title and its Selenium method, the same way a test management tool maps automated tests to test case IDs.

## Two decisions worth explaining

**The captcha is not bypassed in the UI.** The login page uses Cloudflare Turnstile, and Supabase checks the captcha token on the
server too. Clicking through a captcha with automation is flaky and fights the protection the app relies on, so the UI captcha
test is a negative test: it asserts the login stays blocked for an automated browser. A second negative test calls the auth API
without a token and expects `400 captcha_failed`.

**Logged-in tests use an admin magic link instead.** A setup step uses the Supabase service-role key to generate a one-time
magic-link token for the test account, swaps it for a session, and puts that session in `localStorage` where the app expects it.
Playwright does this once and reuses it with `storageState`; Selenium injects it before each test. No captcha is involved because
admin endpoints aren't captcha-protected, and the key never leaves `.env` or the CI secrets.

## Test data rules

The tests run against the live app, on a dedicated test account only.

- Anything a test creates, it removes again (children are added and removed through the UI).
- Settings tests switch a value and switch it back, and only count a step once a fresh reload shows it saved.
- Two tests write data that the app can't remove yet (adding an anushtanam, marking a sandhya slot). They are opt-in with
  `E2E_ALLOW_DATA_WRITES=1` and are skipped in CI.

## Running it locally

Copy `.env.example` to `.env` at the repo root and fill it in. Both suites read the same file.

```
cd playwright-ts
npm ci
npx playwright install chromium
npx playwright test
```

```
cd selenium-java
mvn test
```

Then build the comparison page from the two result files:

```
node dashboard/build.mjs
```

It writes `site/index.html`.

## Layout

```
playwright-ts/
  pages/            page objects (LoginPage, TodayPage, ProfilePage, ...)
  tests/            logged-out tests + auth.setup.ts (magic-link login)
  tests/app/        logged-in tests, one spec per page
  utils/session.ts  magic-link session helper
selenium-java/
  src/test/java/org/nithyakarma/qa/
    base/           BaseTest (driver), LoggedInBaseTest (session injection)
    pages/          the same page objects in Java
    support/        Session (magic link via REST Assured)
    tests/          the same tests, same order (testng.xml)
dashboard/          test case catalogue + the results page generator
.github/workflows/  CI: Playwright, then Selenium, then publish the page
```

## Playwright vs Selenium, from doing it twice

| | Playwright | Selenium |
|---|---|---|
| Waiting | Auto-waiting locators and web-first assertions | Explicit `WebDriverWait` conditions everywhere |
| Locators | `getByRole`, `getByLabel` (accessibility tree) | CSS and XPath only |
| Text | `toHaveText` reads `textContent` | `getText()` returns CSS-transformed text, so the page objects read `textContent` |
| Speed (this suite) | About 35s, parallel workers | About 100s, sequential |
| Ordering | Projects with `dependencies` | `<test>` blocks in `testng.xml` |
| Data-driven | Loop over an array | `@DataProvider` |
| API tests | Built-in `request` fixture | REST Assured |
| Network | `waitForResponse`, `expect.poll` | Polling the page (no network hooks without CDP) |

## Known gaps

- The app has no way to remove a tracked anushtanam yet, so the add test is opt-in until that feature ships.
