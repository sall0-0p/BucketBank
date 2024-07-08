package com.bucketbank.modules.autoupdates;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;

public class UpdateChecker {
    private static final String GITHUB_API_LATEST_RELEASE = "https://api.github.com/repos/sall0-0p/BucketBank/releases/latest";

    public static String getLatestVersion() throws Exception {
        URL url = new URL(GITHUB_API_LATEST_RELEASE);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            

            JSONObject json = new JSONObject(content.toString());
            return json.getString("tag_name");
        } finally {
            connection.disconnect();
        }
        
    };
}
