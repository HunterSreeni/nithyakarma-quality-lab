package org.nithyakarma.qa.tests;

import io.github.cdimascio.dotenv.Dotenv;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class AuthApiTest {
    // api negative test case no browser needed
    private static final Dotenv ENV = Dotenv.configure().directory("..").ignoreIfMissing().load();

    @Test
    public void negativeTestLoginWithApi() {
        String apiUrl = ENV.get("SUPABASE_URL");
        String apiKey = ENV.get("SUPABASE_PUBLISHABLE_KEY");
        if (apiUrl == null || apiKey == null) throw new IllegalStateException("SUPABASE_URL / SUPABASE_PUBLISHABLE_KEY missing from .env");
        String endpoint = apiUrl + "/auth/v1/token?grant_type=password";
        //JSON body as string. \" escapes the quotes inside java strings
        String body = "{\"email\":\"" + ENV.get("E2E_EMAIL") + "\",\"password\":\"" + ENV.get("E2E_PASSWORD") + "\"}";

        Response response = given()
            .header("apikey", apiKey)
            .contentType("application/json")
            .body(body)
        .when()
            .post(endpoint);

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("error_code")).isEqualTo("captcha_failed");
    }
}
