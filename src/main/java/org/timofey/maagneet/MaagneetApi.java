package org.timofey.maagneet;

import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Methods constructing API requests to maagneet site
 */
public class MaagneetApi {
    private static final String API_BASE = "https://middle-api.mag" + "nit.ru";
    private static final String REG_PATH = "/v3/auth/otp";
    private static final String SMS_PATH = "/v3/auth/token";
    private static final String GAME_TOKEN_PATH = "/marketing/v1/webevent/genders";
    private static final String REFRESH_PATH = "/v3/auth/token/refresh";
    private static final String CARD_INFO_PATH = "/v3/authorization";
    private static final String REGISTER_CARD_PATH = "/v3/register";

    private static final String APP_VERSION = "6.36.1";

    private static final String SIGN_HEADER_NAME = "X-Request-Sign";

    // TODO: generate user data
    private static final JSONObject USER_DATA = new JSONObject()
            .put("dateOfBirth", "1991-01-01")
            .put("firstName", "Иван")
            .put("opt-in", new JSONObject()
                    .put("push", false)
                    .put("sms", false)
                    .put("email", false));

    private static final Map<String, String> BASE_HEADERS = new HashMap<>();

    static {
        BASE_HEADERS.put("X-Device-Platform", "Android");
        BASE_HEADERS.put("X-App-Version", APP_VERSION);
        // TODO: Generate device id
        BASE_HEADERS.put("X-Platform-Version", "23");
        BASE_HEADERS.put("User-Agent", "okhttp/4.9.0");
        BASE_HEADERS.put("Accept-Encoding", "deflate");
    }

    private static HttpRequest.Builder getRequestBuilder(String phone, String deviceId, String urlPath) {
        try {
            HttpRequest.Builder builder = HttpRequest
                    .newBuilder(new URI(API_BASE + urlPath))
                    .timeout(Duration.ofSeconds(5));
            for (var headerEntry: BASE_HEADERS.entrySet()) {
                builder = builder.header(headerEntry.getKey(), headerEntry.getValue());
            }
            builder = builder.header("X-Device-Id", deviceId);
            builder = builder.header(SIGN_HEADER_NAME, EncryptGames.generateSign(APP_VERSION, deviceId, phone, urlPath));
            return builder;
        } catch (URISyntaxException e) {
            final String errorMessage = "Some shit with URI";
            System.err.println(errorMessage);
            e.printStackTrace();
            throw new RuntimeException(errorMessage);
        }
    }

    private static HttpRequest.Builder authToken(HttpRequest.Builder builder, MaagneetAccount account) {
        return builder.header("Authorization", "bearer " + account.getToken());
    }

    public static HttpRequest getRegisterRequest(String phone, String deviceId) {
        HttpRequest.Builder builder = getRequestBuilder(phone, deviceId, REG_PATH);
        JSONObject requestBody = new JSONObject().put("phone", phone);
        builder.POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()));
        return builder.build();
    }

    public static HttpRequest getSmsRequest(String phone, String deviceId, String code) {
        // For SMS request sign phone is not used
        HttpRequest.Builder builder = getRequestBuilder("", deviceId, SMS_PATH);
        JSONObject requestBody = new JSONObject()
                .put("phone", phone)
                .put("code", code);
        builder.POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()));
        return builder.build();
    }

    public static HttpRequest getGameTokenRequest(MaagneetAccount account) {
        HttpRequest.Builder builder = getRequestBuilder(account.getPhone(), account.getDeviceId(), GAME_TOKEN_PATH);
        builder = authToken(builder, account);
        return builder.GET().build();
    }

    public static HttpRequest getRefreshRequest(MaagneetAccount account) {
        HttpRequest.Builder builder = getRequestBuilder(account.getPhone(), account.getDeviceId(), REFRESH_PATH);
        JSONObject requestBody = new JSONObject()
                .put("grant_type", "refresh_token")
                .put("refresh_token", account.getRefreshToken());
        builder.POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()));
        return builder.build();
    }

    public static HttpRequest getRegisterCardRequest(MaagneetAccount account) {
        HttpRequest.Builder builder = getRequestBuilder(account.getPhone(), account.getDeviceId(), REGISTER_CARD_PATH);
        builder = authToken(builder, account);
        builder.POST(HttpRequest.BodyPublishers.ofString(USER_DATA.toString()));
        return builder.build();
    }

}
