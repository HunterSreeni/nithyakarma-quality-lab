package org.nithyakarma.qa.support;

import io.github.cdimascio.dotenv.Dotenv;
import io.restassured.response.Response;
import java.net.URI;

import static io.restassured.RestAssured.given;

// Login without the captcha (same idea as playwright-ts/utils/session.ts):
// 1. the service-role (admin) key asks Supabase for a one-time magic-link token
// 2. /verify swaps that token for a real session (access + refresh token)
// 3. LoggedInBaseTest puts the session into localStorage where the app's supabase-js keeps it
public final class Session {
    // local runs read the root .env; CI passes the same values as environment variables
    private static final Dotenv ENV = Dotenv.configure().directory("..").ignoreIfMissing().load();
    private static String shared;

    private Session() {}

    public static String env(String name) {
        String value = ENV.get(name);
        if (value == null || value.isBlank()) throw new IllegalStateException(name + " missing from .env");
        return value;
    }

    // opt-in switches like E2E_ALLOW_DATA_WRITES=1 (from .env or the environment)
    public static boolean flag(String name) {
        return "1".equals(ENV.get(name));
    }

    // one session reused by every logged-in test in the run
    public static synchronized String sharedSessionJson() {
        if (shared == null) shared = createSessionJson();
        return shared;
    }

    public static String createSessionJson() {
        String url = env("SUPABASE_URL");
        String serviceKey = env("SUPABASE_SERVICE_ROLE_KEY");

        Response link = given()
                .header("apikey", serviceKey)
                .header("Authorization", "Bearer " + serviceKey)
                .contentType("application/json")
                .body("{\"type\":\"magiclink\",\"email\":\"" + env("E2E_EMAIL") + "\"}")
            .when()
                .post(url + "/auth/v1/admin/generate_link");
        if (link.statusCode() != 200) throw new IllegalStateException("generate_link failed: " + link.statusCode());
        String hashedToken = link.jsonPath().getString("hashed_token");

        Response verify = given()
                .header("apikey", env("SUPABASE_PUBLISHABLE_KEY"))
                .contentType("application/json")
                .body("{\"type\":\"magiclink\",\"token_hash\":\"" + hashedToken + "\"}")
            .when()
                .post(url + "/auth/v1/verify");
        if (verify.statusCode() != 200) throw new IllegalStateException("verify failed: " + verify.statusCode());
        return verify.asString();
    }

    // supabase-js stores the session under sb-<project-ref>-auth-token
    public static String storageKey() {
        String projectRef = URI.create(env("SUPABASE_URL")).getHost().split("\\.")[0];
        return "sb-" + projectRef + "-auth-token";
    }
}
