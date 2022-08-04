package org.timofey.tools;

public class Utils {
    public static void generateError(String errorPlace, String responseBody, String phone) {
        String errorMessage = "Some shit " + errorPlace;
        System.err.println(errorMessage);
        System.err.println("Phone: " + phone);
        System.err.println("Body: " + responseBody);
        throw new RuntimeException(errorMessage);
    }
}
