package de.sba.discordbot.command;

import com.jagrosh.jdautilities.commandclient.Command;
import com.jagrosh.jdautilities.commandclient.CommandEvent;
import de.sba.discordbot.listener.GameLogService;
import de.sba.discordbot.model.GameLog;
import net.dv8tion.jda.core.entities.Member;
import org.apache.commons.lang3.mutable.MutableLong;

import java.util.Map;
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

    private String millisToString(long millis) {
        long since = millis;
        long hours = TimeUnit.MILLISECONDS.toHours(since);
        since -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(since);
        since -= TimeUnit.MINUTES.toMillis(minutes);
//        long seconds = TimeUnit.MILLISECONDS.toSeconds(since);
        return String.format("%d:%02d", hours, minutes);
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
                            commandEvent.reply(String.format("%s süchtelt seit %s %s",
                                    gameMember.getEffectiveName(),
                                    millisToString(since), gameLog.getGame()));
                        }
                    });
                    break;
                case "today":
                    int diff = 0;
                    if(args.length > 1) {
                        diff = Integer.parseInt(args[1]);
                    }
                    Map<String, Map<String, MutableLong>> today = gameLogService.getToday(diff);
                    StringBuilder msg = new StringBuilder("```markdown\n");
                    today.forEach((userId, userMap) -> {
                        Member gameMember = commandEvent.getGuild().getMemberById(userId);
                        if(gameMember != null) {
                            MutableLong sum = new MutableLong(0);
                            userMap.forEach((game, duration) -> {
                                sum.add(duration);
                                msg.append(String.format("<%s %s> %s\n", gameMember.getEffectiveName(), game, millisToString(duration.longValue())));
                            });
                            msg.append(String.format("* Summe: %s\n", millisToString(sum.longValue())));
                        }
                    });
                    msg.append("\n```");
                    commandEvent.reply(msg.toString());
                    break;
                case "topgame":
                    if(args.length >= 2) {
                        int top = 3;
                        if(args.length > 2) {
                            top = Integer.parseInt(args[2]);
                        }
                        gameLogService.getTopByGame(args[1], top);
                    }
                    break;
                default:
                    commandEvent.reply("Gibts nicht lowl");
            }
        }
    }
}
