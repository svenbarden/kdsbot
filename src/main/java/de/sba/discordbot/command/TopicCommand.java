package de.sba.discordbot.command;

import com.jagrosh.jdautilities.commandclient.Command;
import com.jagrosh.jdautilities.commandclient.CommandEvent;
import de.sba.discordbot.PersistenceService;
import de.sba.discordbot.model.AutoTopic;
import net.dv8tion.jda.core.requests.restaction.AuditableRestAction;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

public class TopicCommand extends Command {
    private PersistenceService persistenceService;

    public TopicCommand(PersistenceService persistenceService) {
        this.persistenceService = persistenceService;
        name = "topic";
        help = "Wer Hilfe braucht ist ein Trottl";
        arguments = "<Neues Topic>";
        guildOnly = true;
    }

    protected String getAutoTopic() {
        Calendar now = Calendar.getInstance();
        int dayOfMonth = now.get(Calendar.DAY_OF_MONTH);
        int month = now.get(Calendar.MONTH);
        List<AutoTopic> topics = persistenceService.findTopics(dayOfMonth, month);
        return topics.stream().map(AutoTopic::getTopic).collect(Collectors.joining(" | "));
    }

    protected boolean register(String topic) {
        Calendar now = Calendar.getInstance();
        int dayOfMonth = now.get(Calendar.DAY_OF_MONTH);
        int month = now.get(Calendar.MONTH);
        return persistenceService.register(dayOfMonth, month, topic);
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        if(commandEvent.getArgs().isEmpty()) {
            commandEvent.replyWarning(String.format("Musst schon Topic angeben um topic zu setzen %s", commandEvent.getAuthor()));
        } else {
            String[] args = commandEvent.getArgs().split("\\s+");
            String newTopic;
            String response = "";
            switch (args[0]) {
                case "auto":
                    newTopic = getAutoTopic();
                    if(newTopic == null || newTopic.length() == 0) {
                        response = "Kein daily topic registriert";
                    } else {
                        response = String.format("Daily topic gesetzt: %s", newTopic);
                    }
                    break;
                case "register":
                    newTopic = StringUtils.arrayToDelimitedString(Arrays.copyOfRange(args, 1, args.length), " ");
                    if(register(newTopic)) {
                        response = String.format("Daily topic gesetzt und registriert: %s", newTopic);
                    } else {
                        response = String.format("Daily topic war schon registriert: %s", newTopic);
                    }
                    break;
                case "set":
                    newTopic = StringUtils.arrayToDelimitedString(Arrays.copyOfRange(args, 1, args.length), " ");
                    response = String.format("Topic gesetzt! Gute Arbeit %s", commandEvent.getAuthor());
                    break;
                default:
                    newTopic = commandEvent.getArgs();
                    response = String.format("Topic gesetzt! Gute Arbeit %s", commandEvent.getAuthor());
            }
            if(newTopic != null) {
                AuditableRestAction<Void> action = commandEvent.getEvent().getTextChannel().getManager().setTopic(newTopic);
                action.submit();
                if (response != null) {
                    commandEvent.getChannel().sendMessage(response);
//                    commandEvent.getChannel().sendMessage(String.format("Topic gesetzt! Gute Arbeit %s", commandEvent.getAuthor()));
//                    commandEvent.replySuccess(String.format("Topic gesetzt! Gute Arbeit %s", commandEvent.getAuthor()));
                }
            }
        }
    }
}
