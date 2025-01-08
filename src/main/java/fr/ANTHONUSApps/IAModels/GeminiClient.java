package fr.ANTHONUSApps.IAModels;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.ANTHONUSApps.Main;
import okhttp3.*;

import java.io.IOException;

public class GeminiClient extends AIClient {

    public GeminiClient() {
        this.API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";
        this.API_KEY = Main.tokenIA;
    }

    @Override
    public String getResponse(String query, String userName) throws IOException {
        JsonObject json = new JsonObject();
        JsonArray contentsArray = new JsonArray();
        JsonObject partsObject = new JsonObject();

        partsObject.addProperty("text", query);

        JsonArray partsArray = new JsonArray();
        partsArray.add(partsObject);
        JsonObject contentObject = new JsonObject();
        contentObject.add("parts", partsArray);
        contentsArray.add(contentObject);

        json.add("contents", contentsArray);

        RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(API_URL + "?key=" + API_KEY)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Erreur API " + response);
            }

            String responseBody = response.body().string();

            JsonObject responseJson = JsonParser.parseString(responseBody).getAsJsonObject();

            JsonArray candidates = responseJson.getAsJsonArray("candidates");
            if (candidates != null && candidates.size() > 0) {
                JsonObject firstCandidate = candidates.get(0).getAsJsonObject();
                JsonObject content = firstCandidate.getAsJsonObject("content");
                JsonArray parts = content.getAsJsonArray("parts");
                if (parts != null && parts.size() > 0) {
                    JsonObject firstPart = parts.get(0).getAsJsonObject();
                    String text = firstPart.get("text").getAsString();
                    return text;
                }
            }

            return "Aucun contenu n'a été généré.";
        }
    }
}
