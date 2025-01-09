package fr.ANTHONUSApps.BotListeners;

import fr.ANTHONUSApps.IAModels.GPTChatMode;
import fr.ANTHONUSApps.Main;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.IOException;

public class MessageListener extends ListenerAdapter {

    private final GPTChatMode gptChatMode;

    public MessageListener() {
        this.gptChatMode = new GPTChatMode();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        /* Mode chat classique
        Seulement dans un salon donné, peut parler avec les autres utilisateurs
        Comportement modifié, et historique des messages sauvegardés.
         */
        if (event.getChannel().getId().equals(Main.channelID)) {
            String userMessage = event.getMessage().getContentRaw();

            chatMode(event, userMessage);

        }

        /* Mode one-prompt TODO
        Faire en sorte que quand le bot est mentionné, il récupère le message après la mention et qu'il fasse une prompt classique de chatgpt
        Peut prendre en charge une taille de messages plus grande que 2000 caractères (split les msg)
        Peut recevoir une photo (si possible)
        Peut envoyer une photo
         */
    }

    private void chatMode(MessageReceivedEvent event, String userMessage) {
        try {
            String gptResponse = gptChatMode.getResponse(userMessage, event.getAuthor().getEffectiveName());

            event.getMessage().reply(gptResponse).queue();
        } catch (IOException e) {
            event.getMessage().reply("Une erreur est survenue lors de la communication avec chat GPT").queue();
            e.printStackTrace();
        }
    }
}
