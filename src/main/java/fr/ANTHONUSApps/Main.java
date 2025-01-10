package fr.ANTHONUSApps;

import fr.ANTHONUSApps.BotListeners.MessageListener;
import fr.ANTHONUSApps.BotListeners.SlashCommandListener;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.managers.AccountManager;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

import java.io.File;
import java.io.IOException;

public class Main extends ListenerAdapter {

    private static final String DEFAULT_BOT_NAME = "JChatBot";
    private static final String DEFAULT_AVATAR_PATH = "Data/default_avatar.png";


    public static String tokenIA;
    public static String channelID;

    public static void main(String[] args) {

        Dotenv dotenv = Dotenv.load();

        //Load ChatGPT api key
        tokenIA = dotenv.get("gpt");
        if (tokenIA == null || tokenIA.isEmpty()) {
            System.out.println("Clé API ChatGPT non trouvé dans le fichier .env");
            return;
        } else {
            System.out.println("Token ChatGPT chargé");
        }


        //Load discord token
        String tokenDiscord = dotenv.get("DISCORD_TOKEN");
        if (tokenDiscord == null || tokenDiscord.isEmpty()) {
            System.out.println("Token Discord non trouvé dans le fichier .env");
            return;
        }

        //Load the bot
        JDA jda = JDABuilder.createDefault(tokenDiscord)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .enableIntents(GatewayIntent.GUILD_MESSAGES)
                .addEventListeners(new MessageListener())
                .addEventListeners(new SlashCommandListener())
                .addEventListeners(new Main())
                .build();

        //Load the slash commands
        CommandListUpdateAction commands = jda.updateCommands();
        commands.addCommands(
                Commands.slash("activate", "Activer le bot dans ce salon (désactivera le bot dans le salon précédent si déjà activé auparavant)"),
                Commands.slash("loadavatar", "Permet de changer la photo de profil du bot")
                        .addOption(OptionType.ATTACHMENT, "fichier", "Fichier de l'image/gif", true),
                Commands.slash("loadname", "Permet de changer le nom du bot")
                        .addOption(OptionType.STRING, "nom", "Nouveau nom du bot", true)
        );
        commands.queue();
    }

    @Override
    public void onReady(ReadyEvent event) {
        AccountManager accountManager = event.getJDA().getSelfUser().getManager();

        accountManager.setName(DEFAULT_BOT_NAME).queue(
                success -> System.out.println("Nom mis à jour avec succès !"),
                error -> System.err.println("Échec de la mise à jour du nom : " + error.getMessage())
        );

        File defaultAvatarFile = new File(DEFAULT_AVATAR_PATH);
        if (defaultAvatarFile.exists()) {
            try {
                Icon defaultAvatar = Icon.from(defaultAvatarFile);
                accountManager.setAvatar(defaultAvatar).queue(
                        success -> System.out.println("Avatar mis à jour avec succès !"),
                        error -> System.err.println("Échec de la mise à jour de l'avatar : " + error.getMessage())
                );
            } catch (IOException e){
                System.err.println("Erreur lors du chargement de l'avatar par défaut : " + e.getMessage());
            }
        } else {
            System.err.println("Fichier avatar par défaut introuvable");
        }
    }
}