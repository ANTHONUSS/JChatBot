package fr.ANTHONUSApps.IAModels;

import com.google.gson.*;
import fr.ANTHONUSApps.Main;
import okhttp3.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class GPTChatMode {

    private final String API_URL;
    private final String API_KEY;
    private final int MAX_TOKENS;

    protected OkHttpClient client;

    private final int MAX_MESSAGES = 10;
    private final int MAX_TOKENS_SUMMARY = 2000;
    private final Path SUMMARY_FILE = Paths.get("gpt-summary.txt");
    private final Path CONTEXT_FILE = Paths.get("gpt-baseContext.txt");

    private final List<JsonObject> messageHistory = new ArrayList<>();
    private String latestSummary = "";
    private String baseContext = "";
    private final String summaryContext = """
            Voici ton but principal en tant qu'IA :
            Tu es une IA utilisée pour assister un bot Discord dans ses conversations avec ses utilisateurs.
            
            - Crée un résumé de la conversation actuelle en combinant le contexte de base du bot discord, le résumé précédents puis enfin l'historique des messages.
            - Le résumé doit inclure le plus d'informations possibles par rapport à l'ancien résumé et l'historique des messages.
            - Le résumé doit être écris du point de vue du bot discord, comme si c'est lui qui l'avais écris pour lui même plus tard.
            - N'hésite pas à faire des résumés d'une certaine taille si il le faut, pour retenir toutes les informations depuis le début de la conversation.
            - Le résumé doit également prendre la façon de parler et la personnalité du bot de base.
            - Fais bien attention d'écrire dans le résumé les noms des utilisateurs ainsi qu'un petit résumé individuel sur chacun d'eux.
            - Écris également bien ce que les utilisateurs ont demandé explicitement de retenir.
            """;

    public GPTChatMode() {
        this.API_URL = "https://api.openai.com/v1/chat/completions";
        this.API_KEY = Main.tokenIA;
        this.MAX_TOKENS = 500;
        this.client = new OkHttpClient();

        try {
            if (Files.exists(SUMMARY_FILE)) {
                latestSummary = Files.readString(SUMMARY_FILE);
                System.out.println("Fichier " + SUMMARY_FILE + " lu, voici son contenu : " + latestSummary);
            }
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement du fichier " + SUMMARY_FILE + ": " + e.getMessage());
        }

        try {
            if (Files.exists(CONTEXT_FILE)) {
                baseContext = Files.readString(CONTEXT_FILE);
                System.out.println("Fichier " + CONTEXT_FILE + " lu, voici son contenu : " + baseContext);
            }
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement du fichier " + CONTEXT_FILE + ": " + e.getMessage());
        }
    }

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

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        System.out.println("Message envoyé à l'IA : " + gson.toJson(json));

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Erreur API " + response);
            }

            String responseBody = response.body().string();
            JsonObject responseJson = JsonParser.parseString(responseBody).getAsJsonObject();

            String assistantResponse = responseJson.getAsJsonArray("choices").get(0).getAsJsonObject()
                    .getAsJsonObject("message").get("content").getAsString();

            addMessageToHistory("assistant", assistantResponse, "Discord Bot");

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

        System.out.println("Message ajouté à l'historique : " + content);
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

        JsonObject contextMessage = new JsonObject();
        contextMessage.addProperty("role", "system");
        contextMessage.addProperty("content", baseContext);
        messages.add(contextMessage);

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

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        System.out.println("Message envoyé à l'IA POUR LE RESUME : " + gson.toJson(json));

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
