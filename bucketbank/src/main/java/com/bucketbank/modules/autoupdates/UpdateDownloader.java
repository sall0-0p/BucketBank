package com.bucketbank.modules.autoupdates;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class UpdateDownloader {
    private static final String PLUGIN_JAR_URL = "https://github.com/sall0-0p/BucketBank/releases/download/{tag}/{filename}.jar";
    private static final String PLUGIN_FILE_NAME = "bucketbank-1.0-Snapshot.jar";

    public static void checkForUpdates(String currentVersion) {
        try {
            String latestVersion = UpdateChecker.getLatestVersion();
            if (!currentVersion.equals(latestVersion)) {
                downloadUpdate(latestVersion);
                // Notify admin or player about the update
            }
        } catch (Exception error) {
            error.printStackTrace();
        }
    }

    private static void downloadUpdate(String version) throws Exception {
        String urlStr = PLUGIN_JAR_URL.replace("{tag}", version).replace("{filename}", "bucketbank-1.0-Snapshot.jar");
        URL url = new URL(urlStr);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        try (InputStream in = connection.getInputStream()) {
            FileOutputStream out = new FileOutputStream("plugins/bucketbank-1.0-Snapshot.jar");
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
        } finally {
            connection.disconnect();
        }
    }
}
