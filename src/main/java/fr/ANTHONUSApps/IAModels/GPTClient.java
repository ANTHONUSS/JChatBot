package fr.ANTHONUSApps.IAModels;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.ANTHONUSApps.Main;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;

public class GPTClient extends AIClient {

    public GPTClient() {
        this.API_URL = "https://api.openai.com/v1/chat/completions";
        this.API_KEY = Main.tokenIA;
    }

    @Override
    public String getResponse(String query) throws IOException {
        JsonObject json = new JsonObject();
        json.addProperty("model", "gpt-4o-mini");
        json.addProperty("max_tokens", 500);

        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", query);

        JsonArray messages = new JsonArray();
        messages.add(userMessage);
        json.add("messages", messages);

        RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {

            if (!response.isSuccessful()) {
                throw new IOException("Erreur API " + response);
            }

            String responseBody = response.body().string();
            JsonObject responseJson = JsonParser.parseString(responseBody).getAsJsonObject();

            return responseJson.getAsJsonArray("choices").get(0).getAsJsonObject()
                    .getAsJsonObject("message").get("content").getAsString();
        }
    }
}
