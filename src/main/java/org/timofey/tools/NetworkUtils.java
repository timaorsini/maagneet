package org.timofey.tools;

import org.json.JSONException;
import org.json.JSONObject;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;

public class NetworkUtils {

    private static final int MAX_REQUEST_RETRIES = 3;

    public static SSLContext getTrustContext() {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
        };

        SSLContext sc;
        try {
            sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            System.err.println("Some shit with trust SSL certificate");
            e.printStackTrace();
            throw new RuntimeException("Some shit with trust SSL certificate");
        }
        return sc;
    }

    public static HttpClient getClient(boolean useBurp) {
        HttpClient.Builder clientBuilder = HttpClient.newBuilder();
        clientBuilder = clientBuilder.connectTimeout(Duration.ofSeconds(5));
        if (useBurp) {
            clientBuilder = clientBuilder
                    .sslContext(NetworkUtils.getTrustContext())
                    .proxy(ProxySelector.of(new InetSocketAddress("127.0.0.1", 8080)));
        }
        return clientBuilder.build();
    }

    /**
     * Rotate mobile proxy on my old unrooted Xiaomi
     */
    public static void restartMobileProxy() throws IOException, InterruptedException {
        System.out.println("Changing ip");
        Runtime.getRuntime().exec("adb -d shell am force-stop com.android.settings").waitFor();
        Runtime.getRuntime().exec("adb -d shell am start -a android.settings.AIRPLANE_MODE_SETTINGS").waitFor();
        Thread.sleep(1000);
        Runtime.getRuntime().exec("adb -d shell input tap 1000 1150").waitFor();
        Thread.sleep(3000);
        Runtime.getRuntime().exec("adb -d shell input tap 1000 1150").waitFor();
        Runtime.getRuntime().exec("adb -d shell am force-stop com.android.settings").waitFor();
        Runtime.getRuntime().exec("adb -d shell am start -n com.android.settings/.TetherSettings").waitFor();
        Thread.sleep(1000);
        Runtime.getRuntime().exec("adb -d shell input tap 1000 510").waitFor();
        Thread.sleep(20000);
        System.out.println("Ip changed");
    }

    public static HttpResponse<String> performRequest(HttpClient client, HttpRequest request, boolean needJson) throws IOException, InterruptedException {
        int retry = 0;
        while (true) {
            try {
                var response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (needJson) {
                    new JSONObject(response.body());
                }
                return response;
            } catch (JSONException | HttpTimeoutException e) {
                retry++;
                System.out.println("Got " + e.getMessage());

                System.out.println("Trying again");
                if (retry == MAX_REQUEST_RETRIES) {
                    throw e;
                }
            }
        }
    }

    public static HttpResponse<String> performRequest(HttpClient client, HttpRequest request) throws IOException, InterruptedException {
        return performRequest(client, request, true);
    }


    public static void main(String[] args) throws IOException, InterruptedException {
        restartMobileProxy();
    }
}
