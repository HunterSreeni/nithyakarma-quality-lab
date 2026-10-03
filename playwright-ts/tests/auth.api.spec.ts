import { test, expect } from '@playwright/test';

test.describe('API negative tests', () => {
    test('Negative test Login with API', async({ request }) => {
        const apiUrl = process.env.SUPABASE_URL;
        const apiKey = process.env.SUPABASE_PUBLISHABLE_KEY;
        if(!apiUrl || !apiKey) throw new Error('SUPABASE_URL / SUPABASE_PUBLISHABLE_KEY missing from .env');
        const endpoint = `${apiUrl}/auth/v1/token?grant_type=password`;
        const response = await request.post(endpoint, {
            headers: { 'apikey': apiKey },
            data: { email: process.env.E2E_EMAIL, password: process.env.E2E_PASSWORD },
        });
        const status = response.status();
        const body = await response.json();

        expect(status).toBe(400);
        expect(body.error_code).toBe('captcha_failed');
    });
});
