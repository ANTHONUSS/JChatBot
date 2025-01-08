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
        if (event.getAuthor().isBot()) return;

        /* Mode chat classique
        Seulement dans un salon donné, peut parler avec les autres utilisateurs
        Comportement modifié, et historique des messages sauvegardés.
         */
        if(event.getChannel().getId().equals(Main.channelID)) {
            String userMessage = event.getMessage().getContentRaw();

            switch (Main.loaded_model) {
                case GEMINI -> {
                    geminiHandle(event, userMessage);
                }
                case GPT -> {
                    gptHandle(event, userMessage);
                }
            }
        }

        /* Mode one_prompt
        Faire en sorte que quand le bot est mentionné, il récupère le message après la mention et qu'il fasse une prompt classique de chatgpt
        Peut prendre en charge une taille de messages plus grande que 2000 caractères (split les msg)
        Peut recevoir une photo (si possible)
        Peut envoyer une photo
         */
    }

    private void geminiHandle(MessageReceivedEvent event, String userMessage) {
        try {
            String geminiResponse = geminiClient.getResponse(userMessage, event.getAuthor().getEffectiveName());

            event.getMessage().reply(geminiResponse).queue();
        } catch (IOException e) {
            event.getMessage().reply("Une erreur est survenue lors de la communication avec Gemini").queue();
            e.printStackTrace();
        }
    }

    private void gptHandle(MessageReceivedEvent event, String userMessage) {
        try {
            String gptResponse = gptClient.getResponse(userMessage, event.getAuthor().getEffectiveName());

            event.getMessage().reply(gptResponse).queue();
        } catch (IOException e) {
            event.getMessage().reply("Une erreur est survenue lors de la communication avec chat GPT").queue();
            e.printStackTrace();
        }
    }
}
