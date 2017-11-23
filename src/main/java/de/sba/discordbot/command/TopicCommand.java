package de.sba.discordbot.command;

import com.jagrosh.jdautilities.commandclient.Command;
import com.jagrosh.jdautilities.commandclient.CommandEvent;
import de.sba.discordbot.model.AutoTopic;
import de.sba.discordbot.service.TopicService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

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
            String newTopic = null;
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
                case "search":
                    LOGGER.trace("execute search");
                    if(args.length < 2) {
                        response = String.format("```\nMusst schon Topic zum suchen angeben %s\n```", commandEvent.getAuthor().getName());
                    } else {
                        String search = StringUtils.arrayToDelimitedString(Arrays.copyOfRange(args, 1, args.length), " ");
                        List<AutoTopic> topics = topicService.search(search);
                        StringBuilder result = new StringBuilder("```\n");
                        if(topics.isEmpty()) {
                            result.append(String.format("Keine Topics für %s gefunden\n", search));
                        } else {
                            topics.forEach(autoTopic -> {
                                result.append(String.format("%02d.%02d.: %s\n", autoTopic.getDayOfMonth(), autoTopic.getMonth(), autoTopic.getTopic()));
                            });
                        }
                        result.append("```");
                        response = result.toString();
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
                commandEvent.getEvent().getTextChannel().getManager().setTopic(newTopic).complete();
            }
            if (response != null) {
                LOGGER.debug("respond with {}", response);
                commandEvent.reply(response);
            }
        }
    }
}
