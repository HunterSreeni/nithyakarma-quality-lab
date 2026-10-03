import type { Page } from '@playwright/test';

// Login without the captcha:
// 1. the service-role (admin) key asks Supabase for a one-time magic-link token
// 2. /verify swaps that token for a real session (access + refresh token)
// 3. the session goes into localStorage, exactly where the app's supabase-js client keeps it
// The captcha only guards the public sign-in endpoints, not the admin ones.

function env(name: string): string {
    const value = process.env[name];
    if (!value) throw new Error(`${name} missing from .env`);
    return value;
}

export async function createSession(): Promise<object> {
    const url = env('SUPABASE_URL');
    const serviceKey = env('SUPABASE_SERVICE_ROLE_KEY');

    const link = await fetch(`${url}/auth/v1/admin/generate_link`, {
        method: 'POST',
        headers: { apikey: serviceKey, Authorization: `Bearer ${serviceKey}`, 'Content-Type': 'application/json' },
        body: JSON.stringify({ type: 'magiclink', email: env('E2E_EMAIL') }),
    });
    if (!link.ok) throw new Error(`generate_link failed: ${link.status}`);
    const { hashed_token } = await link.json();

    const verify = await fetch(`${url}/auth/v1/verify`, {
        method: 'POST',
        headers: { apikey: env('SUPABASE_PUBLISHABLE_KEY'), 'Content-Type': 'application/json' },
        body: JSON.stringify({ type: 'magiclink', token_hash: hashed_token }),
    });
    if (!verify.ok) throw new Error(`verify failed: ${verify.status}`);
    return verify.json();
}

// supabase-js stores the session under sb-<project-ref>-auth-token
export function storageKey(): string {
    const projectRef = new URL(env('SUPABASE_URL')).hostname.split('.')[0];
    return `sb-${projectRef}-auth-token`;
}

export async function loginWithMagicLink(page: Page): Promise<void> {
    const session = await createSession();
    await page.goto('/');
    await page.evaluate(([key, value]) => localStorage.setItem(key, value), [storageKey(), JSON.stringify(session)]);
    await page.reload();
}
