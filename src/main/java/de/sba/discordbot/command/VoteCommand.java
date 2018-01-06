package de.sba.discordbot.command;

import com.jagrosh.jdautilities.commandclient.Command;
import com.jagrosh.jdautilities.commandclient.CommandEvent;
import de.sba.discordbot.exception.PollNotFoundException;
import de.sba.discordbot.service.PollService;
import de.sba.discordbot.util.MessageBuilder;
import de.sba.discordbot.util.MessageType;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VoteCommand extends Command {
	private PollService pollService;

	@Autowired
	public VoteCommand(PollService pollService) {
		this.pollService = pollService;
		name = "vote";
		help = "Wer Hilfe braucht ist ein Trottl";
		arguments = "<Neues Topic>";
		guildOnly = true;
	}

	@Override
	protected void execute(CommandEvent event) {
		String args = event.getArgs();
		if(StringUtils.isBlank(args) || !NumberUtils.isCreatable(args)) {
			event.reply(MessageBuilder.build(MessageType.FORMATTED, "Musst schon angeben für was du votest du Trottl!").toString());
		} else {
			String channel = event.getTextChannel().getId();
			String author = event.getAuthor().getId();
			try {
				pollService.vote(author, channel, Integer.valueOf(args) - 1);
				event.replyFormatted(MessageBuilder.build(MessageType.FORMATTED, "Danke für deinen Vote %s!").toString(), event.getAuthor().getName());
//				event.getMessage().delete().reason("Votes jucken keinen").complete();
			} catch (IndexOutOfBoundsException e) {
				event.replyFormatted(MessageBuilder.build(MessageType.FORMATTED, "Ziemlich unfähig %s, den Eintrag gibts nicht!").toString(), event.getAuthor().getName());
			} catch (PollNotFoundException e) {
				event.replyFormatted(MessageBuilder.build(MessageType.FORMATTED, "Ziemlich unfähig %s, gibt keine Umfrage im Channel!").toString(), event.getAuthor().getName());
			}
		}
	}
}
