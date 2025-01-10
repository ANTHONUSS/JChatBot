package fr.ANTHONUSApps.BotListeners;

import fr.ANTHONUSApps.Main;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.io.IOException;

public class SlashCommandListener extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        switch (event.getName()) {
            case "activate" -> {
                if (event.getChannel().getId().equals(Main.channelID)) {
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
            case "loadavatar" -> {
                OptionMapping option = event.getOption("fichier");

                loadAvatar(event, option);
            }
            case "loadname" -> {
                OptionMapping option = event.getOption("nom");

                loadNom(event, option);
            }
        }
    }

    private void loadAvatar(SlashCommandInteractionEvent event, OptionMapping option) {
        if (option == null || option.getAsAttachment() == null) {
            event.reply("Type du fichier invalide")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        try {

            Message.Attachment attachment = option.getAsAttachment();

            if (!attachment.isImage()) {
                event.reply("Format du fichier invalide")
                        .setEphemeral(true)
                        .queue();
                return;
            }

            attachment.getProxy().download().thenAccept(inputStream -> {
                try {
                    Icon avatarIcon = Icon.from(inputStream);
                    event.getJDA().getSelfUser().getManager().setAvatar(avatarIcon).queue(
                            success -> event.reply("Avatar mis à jour avec succès !").setEphemeral(true).queue(),
                            error -> event.reply("Échec de la mise à jour de l'avatar : " + error.getMessage()).setEphemeral(true).queue()
                    );
                } catch (IOException e) {
                    event.reply("Erreur lors de la mise à jour de l'avatar : " + e.getMessage())
                            .setEphemeral(true)
                            .queue();
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            event.reply("Erreur lors de la gestion de l'avatar : " + e.getMessage())
                    .setEphemeral(true)
                    .queue();
            e.printStackTrace();
        }
    }

    private void loadNom(SlashCommandInteractionEvent event, OptionMapping option) {
        event.getJDA().getSelfUser().getManager().setName(option.getAsString()).queue(
                success -> event.reply("Nom mis à jour avec succès !").setEphemeral(true).queue(),
                error -> event.reply("Échec de la mise à jour du nom : " + error.getMessage()).setEphemeral(true).queue()
        );
    }

}
