package org.timofey.maagneet;

import org.json.JSONArray;
import org.json.JSONObject;
import org.timofey.tools.NetworkUtils;
import org.timofey.tools.Utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class PlayGame {

    private static final String PRIZES_DIR = "prizes";

    private static String getGameToken(HttpClient client, MaagneetAccount account) throws IOException, InterruptedException {
        HttpRequest gameTokenRequest = MaagneetApi.getGameTokenRequest(account);
        HttpResponse<String> gameTokenResponse =
                client.send(gameTokenRequest, HttpResponse.BodyHandlers.ofString());
        JSONObject gameTokenJson = new JSONObject(gameTokenResponse.body());
        if (!gameTokenJson.has("eventKey")) {
            Utils.generateError("game token", gameTokenResponse.body(), account.getPhone());
        }
        return gameTokenJson.getString("eventKey");
    }

    /**
     *
     * @param client
     * @param account
     * @return if the refresh was success (true) or session is already dead (false)
     * @throws IOException
     * @throws InterruptedException
     */
    private static boolean refreshAccountToken(HttpClient client, MaagneetAccount account) throws IOException, InterruptedException {
        HttpRequest refreshTokenRequest = MaagneetApi.getRefreshRequest(account);
        HttpResponse<String> response =
                client.send(refreshTokenRequest, HttpResponse.BodyHandlers.ofString());
        JSONObject refreshJson = new JSONObject(response.body());
        if (refreshJson.has("code") && "auth_error".equals(refreshJson.getString("code"))) {
            return false;
        }
        if (!refreshJson.has("access_token")) {
            Utils.generateError("refresh token", response.body(), account.getPhone());
        }
        account.refreshToken(
                refreshJson.getString("access_token"),
                refreshJson.getString("refresh_token"));
        return true;
    }

    private static void savePrize(JSONObject prizeJson) throws IOException {
        if ("POSTCARD".equals(prizeJson.getString("type"))) {
            System.out.println("Postcard");
        } else {
            String barCode = prizeJson.getJSONObject("barCode").getString("code");
            String category = prizeJson.getString("discount");
            String product = prizeJson.getString("title");
            System.out.println("Prize: " + product + " " + category);
            String prizeDir = String.format("%s/%s/%s/", PRIZES_DIR, category, product);
            new File(prizeDir).mkdirs();
            try (FileWriter writer = new FileWriter(String.format("%s/%s", prizeDir, barCode))) {
                writer.write(prizeJson.toString(4));
            }
        }
    }

    private static void playGame(HttpClient client, String phone, String gameToken, String gameId) throws IOException, InterruptedException {
        HttpRequest gameResultsRequest = GameApi.getGameResultsRequest(gameToken, gameId);
        HttpResponse<String> gameResultsResponse = client.send(gameResultsRequest, HttpResponse.BodyHandlers.ofString());
        JSONObject resultsJson = new JSONObject(gameResultsResponse.body());
        if (!resultsJson.has("prize")) {
            Utils.generateError("getting game result", gameResultsResponse.body(), phone);
        }
        String prizeId = resultsJson.getJSONObject("prize").getString("id");
        HttpRequest prizesRequest = GameApi.getPrizesRequest(gameToken);
        HttpResponse<String> prizesResponse = client.send(prizesRequest, HttpResponse.BodyHandlers.ofString());
        if (!prizesResponse.body().startsWith("[")) {
            Utils.generateError("prizes list", prizesResponse.body(), phone);
        }
        JSONArray allPrizes = new JSONArray(prizesResponse.body());
        JSONObject currentPrize = null;
        for (int i = 0; i < allPrizes.length(); i++) {
            JSONObject prize = allPrizes.getJSONObject(i);
            if (prizeId.equals(prize.getString("id"))) {
                currentPrize = prize;
                break;
            }
        }
        if (currentPrize == null) {
            Utils.generateError("prize with body "+ gameResultsResponse.body(), prizesResponse.body(), phone);
        }
        savePrize(currentPrize);
    }

    /**
     * Returns true if everything OK; false if card is broken
     */
    private static boolean checkGamesAndPlay(HttpClient client, String phone, String gameToken) throws IOException, InterruptedException {
        HttpRequest gameListRequest = GameApi.getGamesListRequest(gameToken);
        HttpResponse<String> response = client.send(gameListRequest, HttpResponse.BodyHandlers.ofString());
        JSONObject gameListJson = new JSONObject(response.body());
        if (gameListJson.has("code")) {
            if ("no_games_available".equals(gameListJson.getString("code"))) {
                System.out.println(phone + " has no games");
            } else if ("invalid_card_number".equals(gameListJson.getString("code"))) {
                return false;
            } else {
                Utils.generateError("games list", response.body(), phone);
            }
        } else {
            if (!gameListJson.has("gameId")) {
                Utils.generateError("games list", response.body(), phone);
            } else {
                String gameId = gameListJson.getString("gameId");
                playGame(client, phone, gameToken, gameId);
            }
        }
        return true;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        boolean useBurp = Boolean.parseBoolean(args[0]);
        HttpClient client = NetworkUtils.getClient(useBurp);
        List<MaagneetAccount> accounts = MaagneetAccount.loadAllAccounts();
        int changeIpCounter = 0;
        for (MaagneetAccount account: accounts) {
            if (changeIpCounter % 10 == 0) {
                System.out.println("Changing proxy");
                NetworkUtils.restartMobileProxy();
            }
            changeIpCounter++;
            System.out.println("Playing with account " + account.getPhone());
            if (!refreshAccountToken(client, account)) {
                account.markDead();
                System.out.println("Auth failed. Marked dead");
                continue;
            }
            String gameToken = getGameToken(client, account);
            if (!checkGamesAndPlay(client, account.getPhone(), gameToken)) {
                System.out.println("Card is broken. Marked dead");
            }
        }
    }
}
