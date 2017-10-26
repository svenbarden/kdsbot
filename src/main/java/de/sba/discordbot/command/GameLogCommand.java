package de.sba.discordbot.command;

import com.jagrosh.jdautilities.commandclient.Command;
import com.jagrosh.jdautilities.commandclient.CommandEvent;
import de.sba.discordbot.listener.GameLogService;
import de.sba.discordbot.model.GameLog;
import net.dv8tion.jda.core.entities.Member;

import java.util.concurrent.TimeUnit;

public class GameLogCommand extends Command {
    private GameLogService gameLogService;

    public GameLogCommand(GameLogService gameLogService) {
        this.gameLogService = gameLogService;
        name = "game";
        help = "Wer Hilfe braucht ist ein Trottl";
        arguments = "<Neues Topic>";
        guildOnly = true;
    }

    private void executeGamePerUser(String userId, String game) {

    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        if(commandEvent.getArgs().isEmpty()) {
            commandEvent.replyWarning(String.format("Musst schon Topic angeben um topic zu setzen %s", commandEvent.getAuthor()));
        } else {
            String[] args = commandEvent.getArgs().split("\\s+");
            switch (args[0]) {
                case "list":
                    commandEvent.getGuild().getMembers().stream().filter(member -> member.getGame() != null).forEach(member -> {
                        GameLog gameLog = gameLogService.getForUser(member.getUser().getId());
                        if(gameLog != null) {
                            Member gameMember = commandEvent.getGuild().getMemberById(gameLog.getUser());
                            long since = System.currentTimeMillis() - gameLog.getStart().getTime();
                            long hours = TimeUnit.MILLISECONDS.toHours(since);
                            commandEvent.reply(String.format("%s süchtelt seit %s %s", gameMember.getEffectiveName(), gameLog.getStart(), gameLog.getGame()));
                        }
                    });
                    break;
                default:
                    commandEvent.reply("Gibts nicht lowl");
            }
        }
    }
}
