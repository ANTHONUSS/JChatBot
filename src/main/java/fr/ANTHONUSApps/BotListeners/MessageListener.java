package fr.ANTHONUSApps.BotListeners;

import fr.ANTHONUSApps.IAModels.GPTClient;
import fr.ANTHONUSApps.IAModels.GeminiClient;
import fr.ANTHONUSApps.Main;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.IOException;

public class MessageListener extends ListenerAdapter {

    private final GeminiClient geminiClient;
    private final GPTClient gptClient;

    public MessageListener() {
        this.geminiClient = new GeminiClient();
        this.gptClient = new GPTClient();
    }

    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }
        String userMessage = event.getMessage().getContentRaw();

        switch (Main.loaded_model){
            case GEMINI -> {
                geminiHandle(event, userMessage);
            }
            case GPT -> {
                gptHandle(event, userMessage);
            }
        }
    }

    private void geminiHandle(MessageReceivedEvent event, String userMessage) {
        try {
            String geminiResponse = geminiClient.getResponse(userMessage);

            event.getMessage().reply(geminiResponse).queue();
        } catch (IOException e) {
            event.getMessage().reply("Une erreur est survenue lors de la communication avec Gemini").queue();
            e.printStackTrace();
        }
    }

    private void gptHandle(MessageReceivedEvent event, String userMessage) {
        try {
            String geminiResponse = gptClient.getResponse(userMessage);

            event.getMessage().reply(geminiResponse).queue();
        } catch (IOException e) {
            event.getMessage().reply("Une erreur est survenue lors de la communication avec chat GPT").queue();
            e.printStackTrace();
        }
    }
}
