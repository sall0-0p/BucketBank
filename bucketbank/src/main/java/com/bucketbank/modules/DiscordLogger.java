package com.bucketbank.modules;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.bukkit.configuration.file.FileConfiguration;

import com.bucketbank.App;

public class DiscordLogger {
    App plugin = App.getPlugin();
    FileConfiguration config = plugin.getConfig();

    public void log(String webhookType, String content) {
        String url = config.getString("webhook." + webhookType + ".url");
        String color = config.getString("webhook." + webhookType + ".color");
        String title = config.getString("webhook." + webhookType + ".title"); 

        sendWebhook(url, content, title, color);
    }

    private void sendWebhook(String webhookUrl, String content, String title, String color) {
        if (webhookUrl == null || webhookUrl.isEmpty()) {
            System.out.println("Webhook URL is not set for this message type.");
            return;
        }

        try {
            URL url = new URL(webhookUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            String jsonPayload = "{\"content\":null,\"embeds\":[{\"title\":\"" + title + "\",\"description\":\"" + content + "\",\"color\":" + color + "}],\"attachments\":[]}";
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode != 204) {
                plugin.getLogger().info("204 error!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
