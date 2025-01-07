package fr.ANTHONUSApps;

import fr.ANTHONUSApps.BotListeners.MessageListener;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class Main {
    public static String tokenDiscord;
    public static String tokenIA;
    public static Models loaded_model;

    public enum Models {
        GEMINI, GPT
    }

    public static void main(String[] args) {

        if(args.length == 0) {
            System.out.println("Aucun argument rentré");
            return;
        }

        String arg = args[0];
        switch (arg) {
            case "gemini" -> {
                loadGemini();
                if (tokenIA == null || tokenIA.isEmpty()) {
                    System.out.println("Clé API ChatGPT non trouvé dans le fichier .env");
                    return;
                } else {
                    loaded_model= Models.GEMINI;
                    System.out.println("Token Gemini Chargé");
                }
            }
            case "gpt" -> {
                loadGPT();
                if (tokenIA == null || tokenIA.isEmpty()) {
                    System.out.println("Clé API ChatGPT non trouvé dans le fichier .env");
                    return;
                } else {
                    loaded_model= Models.GPT;
                    System.out.println("Token ChatGPT chargé");
                }
            }
            default -> {
                System.out.println("Argument " + arg + " non valide");
                return;
            }
        }

        //Load discord token
        Dotenv dotenvDiscord = Dotenv.load();
        tokenDiscord = dotenvDiscord.get("DISCORD_TOKEN");
        if (tokenDiscord == null || tokenDiscord.isEmpty()) {
            System.out.println("Token Discord non trouvé dans le fichier .env");
            return;
        }
        //Load the bot
        loadBot();
    }

    private static void loadBot() {
        JDA jda = JDABuilder.createDefault(tokenDiscord)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .enableIntents(GatewayIntent.GUILD_MESSAGES)
                .addEventListeners(new MessageListener())
                .build();
    }

    private static void loadGemini() {
        //Load gemini api key
        Dotenv dotenvGemini = Dotenv.load();
        tokenIA = dotenvGemini.get("gemini");
    }

    private static void loadGPT() {
        //Load ChatGPT api key
        Dotenv dotenvGemini = Dotenv.load();
        tokenIA = dotenvGemini.get("gpt");
    }
}