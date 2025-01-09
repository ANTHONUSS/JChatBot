package fr.ANTHONUSApps;

import fr.ANTHONUSApps.BotListeners.MessageListener;
import fr.ANTHONUSApps.BotListeners.SlashCommandListener;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

public class Main {
    public static String tokenIA;
    public static String channelID;
    public static Models loaded_model;

    private static Dotenv dotenv;

    public enum Models {
        GEMINI, GPT
    }

    public static void main(String[] args) {

        if (args.length == 0) {
            System.out.println("Aucun argument rentré");
            return;
        }

        dotenv = Dotenv.load();

        String arg = args[0];
        switch (arg) {
            case "gemini" -> {
                //Load gemini api key
                tokenIA = dotenv.get("gemini");
                if (tokenIA == null || tokenIA.isEmpty()) {
                    System.out.println("Clé API ChatGPT non trouvé dans le fichier .env");
                    return;
                } else {
                    loaded_model = Models.GEMINI;
                    System.out.println("Token Gemini Chargé");
                }
            }
            case "gpt" -> {
                //Load ChatGPT api key
                tokenIA = dotenv.get("gpt");
                if (tokenIA == null || tokenIA.isEmpty()) {
                    System.out.println("Clé API ChatGPT non trouvé dans le fichier .env");
                    return;
                } else {
                    loaded_model = Models.GPT;
                    System.out.println("Token ChatGPT chargé");
                }
            }
            default -> {
                System.out.println("Argument " + arg + " non valide");
                return;
            }
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
                .build();

        //Load the slash commands
        CommandListUpdateAction commands = jda.updateCommands();
        commands.addCommands(
                Commands.slash("activate", "Activer le bot dans ce salon (désactivera le bot dans le salon précédent si déjà activé auparavant)")
        );
        commands.queue();
    }
}