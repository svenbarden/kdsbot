package de.sba.discordbot.command;

import com.jagrosh.jdautilities.commandclient.Command;
import com.jagrosh.jdautilities.commandclient.CommandEvent;
import de.sba.discordbot.service.TopicService;
import net.dv8tion.jda.core.requests.RequestFuture;
import net.dv8tion.jda.core.requests.restaction.AuditableRestAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TopicCommand extends Command {
    private static final Logger LOGGER = LoggerFactory.getLogger(TopicCommand.class);
    private TopicService topicService;

    public TopicCommand(TopicService topicService) {
        this.topicService = topicService;
        name = "topic";
        help = "Wer Hilfe braucht ist ein Trottl";
        arguments = "<Neues Topic>";
        guildOnly = true;
    }

    protected String getAutoTopic() {
        return topicService.getAutoTopic();
    }

    protected boolean register(String topic) {
        Calendar now = Calendar.getInstance();
        int dayOfMonth = now.get(Calendar.DAY_OF_MONTH);
        int month = now.get(Calendar.MONTH);
        return topicService.register(dayOfMonth, month, topic);
    }

    protected void reload() {
        topicService.reload();
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        LOGGER.debug("receive command args {}", commandEvent.getArgs());
        if(commandEvent.getArgs().isEmpty()) {
            commandEvent.replyWarning(String.format("Musst schon Topic angeben um topic zu setzen %s", commandEvent.getAuthor().getName()));
        } else {
            String[] args = commandEvent.getArgs().split("\\s+");
            String newTopic;
            String response = "";
            switch (args[0]) {
                case "reload":
                    LOGGER.trace("execute reload");
                    reload();
                case "auto":
                    LOGGER.trace("execute auto");
                    newTopic = getAutoTopic();
                    if(newTopic == null || newTopic.length() == 0) {
                        response = "Kein daily topic registriert";
                    } else {
                        response = String.format("```\nDaily topic gesetzt: %s\n```", newTopic);
                    }
                    break;
                case "register":
                    LOGGER.trace("execute register");
                    newTopic = StringUtils.arrayToDelimitedString(Arrays.copyOfRange(args, 1, args.length), " ");
                    if(register(newTopic)) {
                        response = String.format("```\nDaily topic gesetzt und registriert: %s\n```", newTopic);
                    } else {
                        response = String.format("```\nDaily topic war schon registriert: %s\n```", newTopic);
                    }
                    break;
                case "set":
                    LOGGER.trace("execute set");
                    newTopic = StringUtils.arrayToDelimitedString(Arrays.copyOfRange(args, 1, args.length), " ");
                    response = String.format("```\nTopic gesetzt! Gute Arbeit %s\n```", commandEvent.getAuthor().getName());
                    break;
                default:
                    newTopic = commandEvent.getArgs();
                    response = String.format("```\nTopic gesetzt! Gute Arbeit %s\n```", commandEvent.getAuthor().getName());
            }
            if(newTopic != null) {
                AuditableRestAction<Void> action = commandEvent.getEvent().getTextChannel().getManager().setTopic(newTopic);
                RequestFuture<Void> submit = action.submit();
                try {
                    submit.get(5, TimeUnit.SECONDS);
                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    e.printStackTrace();
                }
                if (response != null) {
                    LOGGER.debug("respond with {}", response);
//                    commandEvent.getEvent().getTextChannel().sendMessage(response);
                    commandEvent.reply(response);
//                    commandEvent.getChannel().sendMessage(String.format("Topic gesetzt! Gute Arbeit %s", commandEvent.getAuthor()));
//                    commandEvent.replySuccess(String.format("Topic gesetzt! Gute Arbeit %s", commandEvent.getAuthor()));
                }
            }
        }
    }
}
