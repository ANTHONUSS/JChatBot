package fr.ANTHONUSApps.BotListeners;

import fr.ANTHONUSApps.Main;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class SlashCommandListener extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        switch (event.getName()){
            case "activate" -> {
                if(event.getChannel().getId().equals(Main.channelID)){
                    event.reply("Le channel est déjà activé.")
                            .setEphemeral(true)
                            .queue();
                    return;
                }

                Main.channelID = event.getChannel().getId();
                event.reply("Channel activé pour le bot.")
                        .setEphemeral(true)
                        .queue();
            }
        }
    }

}
