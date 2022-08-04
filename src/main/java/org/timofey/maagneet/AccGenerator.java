package org.timofey.maagneet;

import org.json.JSONObject;
import org.timofey.sms_activate.ActivationService;
import org.timofey.sms_activate.SMSActivateApi;
import org.timofey.sms_activate.client_enums.SMSActivateClientStatus;
import org.timofey.sms_activate.error.base.SMSActivateBaseException;
import org.timofey.sms_activate.response.api_activation.SMSActivateActivation;
import org.timofey.sms_activate.response.api_activation.enums.SMSActivateGetStatusActivation;
import org.timofey.tools.NetworkUtils;
import org.timofey.tools.Utils;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;

public class AccGenerator implements Runnable {

    private static final boolean USE_BURP = false;
    private static final int MAX_SMS_DELAY = 25;

    private String vakApi;

    public AccGenerator(String vakApi) {
        this.vakApi = vakApi;
    }

    public static void checkMaagneetCard(HttpClient client, MaagneetAccount account) throws IOException, InterruptedException {
        var registerCardResponse = client.send(MaagneetApi.getRegisterCardRequest(account),
                HttpResponse.BodyHandlers.ofString());
        JSONObject registerCardJson = new JSONObject(registerCardResponse.body());
        if (!(registerCardJson.has("code") && "user_already_exists".equals(registerCardJson.getString("code"))
                || registerCardJson.has("user"))) {
            Utils.generateError("card info", registerCardResponse.body(), account.getPhone());
        }
    }

    private static HttpClient sendAccountRegisterRequest(HttpClient client, String phone,
                                                         String deviceId, boolean proxyAlreadyRestarted)
            throws IOException, InterruptedException {
        HttpResponse<String> regResponse = NetworkUtils.performRequest(client,
                MaagneetApi.getRegisterRequest(phone, deviceId));
        JSONObject regJson = new JSONObject(regResponse.body());
        if (!proxyAlreadyRestarted && regJson.has("code")
                && "account_suspended".equals(regJson.getString("code"))) {
            System.out.println("Need to change proxy");
            NetworkUtils.restartMobileProxy();
            client = NetworkUtils.getClient(USE_BURP);
            sendAccountRegisterRequest(client, phone, deviceId, true);
        } else if (!regJson.has("expires")) {
            Utils.generateError("register", regResponse.body(), phone);
        }
        return client;
    }


    public void test() throws SMSActivateBaseException, InterruptedException {
        SMSActivateApi vakApi = new SMSActivateApi(ActivationService.VAKSMS, this.vakApi);
        SMSActivateActivation activation = vakApi.getNumber(0, "mg");
        String phone = Long.toString(activation.getNumber());
        System.out.println("Registering " + phone);
        int smsDelay = 0;
        while (vakApi.getStatus(activation).getSMSActivateGetStatus() == SMSActivateGetStatusActivation.WAIT_CODE) {
            System.out.println("Waiting...");
            Thread.sleep(1000);
            smsDelay++;
            if (smsDelay > MAX_SMS_DELAY) {
                System.out.println("Number was cancelled. Timeout");
                vakApi.setStatus(activation, SMSActivateClientStatus.CANCEL);
            }
        }
        System.out.println(vakApi.getStatus(activation).getCodeFromSMS());;
    }

    public void generate(boolean manualPhones) throws SMSActivateBaseException, IOException, InterruptedException {
        Scanner scanner = new Scanner(System.in);
        SMSActivateApi vakApi = new SMSActivateApi(ActivationService.VAKSMS, this.vakApi);
        String phone;
        SMSActivateActivation activation = null;
        if (manualPhones) {
            System.out.print("Phone: ");
            phone = scanner.nextLine().strip();
        } else {
            activation = vakApi.getNumber(0, "mg", false,
                    new HashSet<>(Set.of("beeline")), null);
            phone = Long.toString(activation.getNumber());
            System.out.println("Registering " + phone);
        }
        String deviceId = UUID.randomUUID().toString();
        HttpClient client = NetworkUtils.getClient(USE_BURP);
        client = sendAccountRegisterRequest(client, phone, deviceId, false);
        MaagneetAccount account = new MaagneetAccount(phone);
        String code;
        if (manualPhones) {
            System.out.print("Code: ");
            code = scanner.nextLine().strip();
        } else {
            int smsDelay = 0;
            boolean timeOut = false;
            while (vakApi.getStatus(activation).getSMSActivateGetStatus() == SMSActivateGetStatusActivation.WAIT_CODE) {
                Thread.sleep(1000);
                smsDelay++;
                if (smsDelay == MAX_SMS_DELAY) {
                    System.out.println("Number was cancelled. Timeout");
                    vakApi.setStatus(activation, SMSActivateClientStatus.CANCEL);
                    timeOut = true;
                    break;
                }
                if (smsDelay % 10 == 0) {
                    System.out.println("Waiting...");
                }
            }
            if (timeOut) {
                return;
            }
            code = vakApi.getStatus(activation).getCodeFromSMS();
        }
        System.out.print(code);
        HttpResponse<String> smsResponse = client.send(
                MaagneetApi.getSmsRequest(phone, deviceId, code),
                HttpResponse.BodyHandlers.ofString());
        JSONObject smsJson = new JSONObject(smsResponse.body());
        if (smsJson.has("code")) {
            String errorCode = smsJson.getString("code");
            if ("invalid_otp_code".equals(errorCode)) {
                System.out.println("Invalid sms, trying again");
            } else {
                System.err.println("Unknown code in response body");
                System.err.println("Body " + smsResponse.body());
                throw new RuntimeException();
            }
        } else {
            JSONObject smsInternalJson = smsJson.getJSONObject("mag" + "nit-api");
            account.setToken(smsInternalJson.getString("access_token"));
            account.setTokenTimestamp(System.currentTimeMillis());
            account.setRefreshToken(smsInternalJson.getString("refresh_token"));
            account.setDeviceId(deviceId);
        }
        checkMaagneetCard(client, account);
        account.save();
    }

    @Override
    public void run() {

    }
}
