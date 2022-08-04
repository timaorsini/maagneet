package org.timofey.maagneet;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MaagneetAccount {

    private static final String ACCOUNT_DIR = "accounts";
    private static final String DEAD_ACCOUNT_DIR = "dead_accounts";

    private static final Gson GSON = new Gson();

    private String phone;
    private String token;
    private long tokenTimestamp;
    private String refreshToken;
    private String deviceId;

    public MaagneetAccount(String phone) {
        this.phone = phone;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public long getTokenTimestamp() {
        return tokenTimestamp;
    }

    public void setTokenTimestamp(long tokenTimestamp) {
        this.tokenTimestamp = tokenTimestamp;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    private String getFilename() {
        return phone + ".json";
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public void save() {
        try (JsonWriter writer = new JsonWriter(new FileWriter(ACCOUNT_DIR + "/" + getFilename()))) {
            GSON.toJson(this, MaagneetAccount.class, writer);
        } catch (IOException e) {
            String errorMessage = "Some shit with saving account";
            System.err.println(errorMessage);
            e.printStackTrace();
            throw new RuntimeException(errorMessage);
        }
    }

    public static List<MaagneetAccount> loadAllAccounts() {
        File accountsDir = new File(ACCOUNT_DIR);
        List<MaagneetAccount> accounts = new ArrayList<>();
        for (File accountFile: Objects.requireNonNull(accountsDir.listFiles())) {
            try (JsonReader reader = new JsonReader(new FileReader(accountFile))) {
                accounts.add(GSON.fromJson(reader, MaagneetAccount.class));
            } catch (IOException e) {
                String errorMessage = "Some shit with reading accounts";
                System.err.println(errorMessage);
                e.printStackTrace();
                throw new RuntimeException(errorMessage);
            }
        }
        return accounts;
    }

    public void refreshToken(String newToken, String newRefreshToken) {
        this.token = newToken;
        this.refreshToken = newRefreshToken;
        this.tokenTimestamp = System.currentTimeMillis();
        save();
    }

    public void markDead() throws IOException {
        String filename = getFilename();
        Files.move(new File(ACCOUNT_DIR + "/" + filename),
                new File(DEAD_ACCOUNT_DIR + "/" + filename));

    }

}
