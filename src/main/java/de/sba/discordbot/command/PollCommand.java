package de.sba.discordbot.command;

import com.jagrosh.jdautilities.commandclient.Command;
import com.jagrosh.jdautilities.commandclient.CommandEvent;

public class PollCommand extends Command {

    public PollCommand() {
        name = "poll";
        help = "Wer Hilfe braucht ist ein Trottl";
        arguments = "<Neues Topic>";
        guildOnly = true;
    }

    @Override
    protected void execute(CommandEvent event) {
        String[] args = event.getArgs().split("\n");
        if(args.length < 3) {
            event.reply("Brauchst schon 2 Optionen für nene Poll du Mongo");
        } else {

        }
    }
}
