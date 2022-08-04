package org.timofey.maagneet;

import org.json.JSONObject;

import java.io.*;
import java.util.Objects;
import java.util.Scanner;

public class SetDeviceIds {

    public static void main(String[] args) throws IOException {
        for (File file: Objects.requireNonNull(new File("checked 0213").listFiles())) {
            JSONObject json;
            try (Scanner inputScanner = new Scanner(file)) {
                json = new JSONObject(inputScanner.nextLine().strip());
            }
            json.put("deviceId", "600baf36-9900-4a57-87ef-fc4eef4c6bc9");
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(json.toString());
            }
        }
    }
}
