package de.sba.discordbot.command;

import com.jagrosh.jdautilities.commandclient.Command;
import com.jagrosh.jdautilities.commandclient.CommandEvent;
import de.sba.discordbot.model.GameLog;
import de.sba.discordbot.model.GameLogFilter;
import de.sba.discordbot.model.GameLogResult;
import de.sba.discordbot.service.GameLogService;
import de.sba.discordbot.util.DateTimeUtils;
import de.sba.discordbot.util.MessageBuilder;
import de.sba.discordbot.util.MessageType;
import de.sba.discordbot.util.UserUtils;
import net.dv8tion.jda.core.entities.Member;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.mutable.MutableLong;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GameLogCommand extends Command {
    private GameLogService gameLogService;

    @Autowired
    public GameLogCommand(GameLogService gameLogService) {
        this.gameLogService = gameLogService;
        name = "game";
        help = "Wer Hilfe braucht, ist ein Trottl!";
        arguments = "<Neues Topic>";
        guildOnly = true;
    }

    private String millisToString(long millis) {
        return DateTimeUtils.millisToString(millis);
    }

    private List<String> buildGameLogResultString(CommandEvent commandEvent, GameLogResult result) {
        MessageBuilder msg = MessageBuilder.build(MessageType.MARKDOWN);
        msg.append("Von ").append(ObjectUtils.defaultIfNull(result.getFrom(), "Anfang"))
                .append(" bis ").append(result.getTo()).append("\n").nextPart();
        result.getData().forEach((userId, userMap) -> {
            Member gameMember = commandEvent.getGuild().getMemberById(userId);
            if(gameMember != null) {
                MutableLong sum = new MutableLong(0);
                userMap.forEach((game, duration) -> {
                    sum.add(duration);
                    msg.append(String.format("<%s %s> %s\n", gameMember.getEffectiveName(), game, millisToString(duration.longValue())));
                });
                msg.append(String.format("* Summe: %s\n", millisToString(sum.longValue()))).nextPart();
            }
        });
        return msg.toLimitedString();
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        if(commandEvent.getArgs().isEmpty()) {
            commandEvent.reply(MessageBuilder.build(MessageType.FORMATTED, String.format("Musst schon Topic angeben, um Topic zu setzen, %s.",
                                                                                         commandEvent.getAuthor().getName())).toString());
        } else {
            String[] args = commandEvent.getArgs().split("\\s+");
            switch (args[0]) {
                case "list":
                    commandEvent.getGuild().getMembers().stream().filter(member -> member.getGame() != null).forEach(member -> {
                        GameLog gameLog = gameLogService.getForUser(member.getUser().getId());
                        if(gameLog != null) {
                            Member gameMember = commandEvent.getGuild().getMemberById(gameLog.getUser());
                            long since = System.currentTimeMillis() - gameLog.getStart().getTime();
                            commandEvent.reply(String.format("%s süchtelt seit %s %s.",
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
                    buildGameLogResultString(commandEvent, gameLogService.getToday(diff)).forEach(commandEvent::reply);
                    break;
                case "all":
                    GameLogFilter filter = GameLogFilter.build(args, 1, (userName) -> UserUtils.findUsers(commandEvent.getGuild(), userName));
                    buildGameLogResultString(commandEvent, gameLogService.getAll(filter)).forEach(commandEvent::reply);
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
                    commandEvent.reply(MessageBuilder.build(MessageType.FORMATTED, "Gibts nicht, lowl.").toString());
            }
        }
    }
}
