package org.timofey.maagneet;

import java.net.URI;
import java.net.http.HttpRequest;

/**
 * Methods constructing API requests to the game
 */
public class GameApi {

    private static final String BASE_URL = "https://mag" + "nit-mg03-webapp.apps.prd.west" + "europe.bright-" +
            "shopper.com";

    private static HttpRequest getRequest(String gameToken, String urlPath) {
        return HttpRequest.newBuilder(URI.create(BASE_URL + urlPath))
                .header("Authorization", "Bearer " + gameToken)
                .POST(HttpRequest.BodyPublishers.ofString(""))
                .build();
    }

    public static HttpRequest getGamesListRequest(String gameToken) {
        return getRequest(gameToken, "/3scale-api/v2/games");
    }

    public static HttpRequest getGameResultsRequest(String gameToken, String gameId) {
        return getRequest(gameToken, "/3scale-api/v2/games/" + gameId + "/play");
    }

    public static HttpRequest getPrizesRequest(String gameToken) {
        return HttpRequest.newBuilder(URI.create(BASE_URL + "/3scale-api/v2/prizes"))
                .header("Authorization", "Bearer " + gameToken)
                .GET()
                .build();
    }

}
