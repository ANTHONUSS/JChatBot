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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class GPTClient extends AIClient {

    private final int MAX_MESSAGES = 10;
    private final int MAX_TOKENS_SUMMARY = 2000;
    private final Path SUMMARY_FILE = Paths.get("gpt-summary.json");

    private final List<JsonObject> messageHistory = new ArrayList<>();
    private String latestSummary = "";

    private final String baseContext = """
            Tu es un bot Discord créé pour parler avec des utilisateurs de Discord.
            Tu parleras comme une personne d'environ 20 ans.
            Tu répondras uniquement en français sauf si on te demande explicitement de parler dans une autre langue.
            Tu essaieras d'engager des discussions ou répondre à ce qu'on te dit.
            Essaie de prendre le ton de mes amis, on aime beaucoup l'humour noire, et dès qu'on s'insulte c'est souvent pour rigoler.
            """;
    private final String summaryContext = """
            Tu es une IA utilisée pour assister un bot Discord dans ses conversations avec ses utilisateurs.
            Crée un résumé de la conversation actuelle en combinant l'historique des messages récents et le résumé précédent.
            Le résumé doit inclure les points importants de la conversation, les sujets abordés, et les informations spécifiques que les utilisateurs ont demandé à retenir.
            Soit concis dans le résumé, ne garde que les informations essentielles et n'écris pas des messages trop gros.
            """;

    public GPTClient() {
        this.API_URL = "https://api.openai.com/v1/chat/completions";
        this.API_KEY = Main.tokenIA;
        this.MAX_TOKENS = 500;

        try {
            if (Files.exists(SUMMARY_FILE)) latestSummary = Files.readString(SUMMARY_FILE);
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement du fichier " + SUMMARY_FILE + ": " + e.getMessage());
        }
    }

    @Override
    public String getResponse(String query, String userName) throws IOException {
        addMessageToHistory("user", query, userName);

        JsonObject json = new JsonObject();
        json.addProperty("model", "gpt-4o-mini");
        json.addProperty("max_tokens", MAX_TOKENS);

        JsonArray messages = buildMessageContext();
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

            String assistantResponse = responseJson.getAsJsonArray("choices").get(0).getAsJsonObject()
                    .getAsJsonObject("message").get("content").getAsString();

            addMessageToHistory("assistant", assistantResponse, null);

            if (messageHistory.size() > MAX_MESSAGES) generateSummary();

            return assistantResponse;
        }
    }

    private void addMessageToHistory(String role, String content, String userName) {
        JsonObject message = new JsonObject();
        message.addProperty("role", role);

        if (role.equals("user") && userName != null) content = userName + ": " + content;
        message.addProperty("content", content);
        messageHistory.add(message);
    }

    private JsonArray buildMessageContext() {
        JsonArray messages = new JsonArray();

        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", "system");
        systemMessage.addProperty("content", baseContext);
        messages.add(systemMessage);

        if (!latestSummary.isEmpty()) {
            JsonObject summaryMessage = new JsonObject();
            summaryMessage.addProperty("role", "system");
            summaryMessage.addProperty("content", latestSummary);
            messages.add(summaryMessage);
        }

        for (JsonObject message : messageHistory) messages.add(message);

        return messages;
    }

    private void generateSummary() throws IOException {
        JsonObject json = new JsonObject();
        json.addProperty("model", "gpt-4o-mini");
        json.addProperty("max_tokens", MAX_TOKENS_SUMMARY);

        JsonArray messages = new JsonArray();

        JsonObject summaryMessage = new JsonObject();
        summaryMessage.addProperty("role", "system");
        summaryMessage.addProperty("content", summaryContext);
        messages.add(summaryMessage);

        if (!latestSummary.isEmpty()) {
            JsonObject previousSummary = new JsonObject();
            previousSummary.addProperty("role", "system");
            previousSummary.addProperty("content", latestSummary);
            messages.add(previousSummary);
        }

        for (JsonObject message : messageHistory) messages.add(message);

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

            latestSummary = responseJson.getAsJsonArray("choices").get(0).getAsJsonObject()
                    .getAsJsonObject("message").get("content").getAsString();

            Files.writeString(SUMMARY_FILE, latestSummary, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            messageHistory.clear();
        }

    }
}
