package edu.upc.shadowcode;

import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Client {
    private static final String serverAddress = "http://shadowcode.azurewebsites.net/api/";

    public static JSONObject request(String name, JSONObject data) {
        try {
            URL url = new URL(serverAddress + name);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);
            connection.setDoInput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setRequestProperty("Content-Type", "application/json");

            OutputStream writer = connection.getOutputStream();
            writer.write(data.toString().getBytes(StandardCharsets.UTF_8));
            writer.flush();
            writer.close();

            InputStream input = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
            String line = "";
            StringBuilder result = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            reader.close();
            input.close();
            String text = result.toString();
            text = text.replace("\\", "");
            text = text.substring(text.indexOf("{"), text.lastIndexOf("}") + 1);
            return new JSONObject(text);
        } catch (Exception e) {
            Log.d("UserModel", e.toString());

            return null;
        }
    }
}
