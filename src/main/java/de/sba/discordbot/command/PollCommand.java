package de.sba.discordbot.command;

import com.jagrosh.jdautilities.commandclient.Command;
import com.jagrosh.jdautilities.commandclient.CommandEvent;
import de.sba.discordbot.model.Poll;
import de.sba.discordbot.model.Vote;
import de.sba.discordbot.service.PollService;
import de.sba.discordbot.util.DateTimeUtils;
import de.sba.discordbot.util.MessageBuilder;
import de.sba.discordbot.util.MessageType;
import de.sba.discordbot.util.UserUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class PollCommand extends Command {
    private PollService pollService;

    @Autowired
    public PollCommand(PollService pollService) {
        this.pollService = pollService;
        name = "poll";
        help = "Wer Hilfe braucht ist ein Trottl";
        arguments = "<Neues Topic>";
        guildOnly = true;
    }

    private String voteToString(String option, List<Vote> votes, int index) {
    	StringBuilder s = new StringBuilder("* ").append(index + 1).append(": ");
    	if(votes == null || votes.isEmpty()) {
    		s.append("0");
	    } else {
    		s.append(votes.size());
	    }
	    return s.append(" Votes => ").append(option).append("\n").toString();
    }

    private String pollToString(CommandEvent event, Poll poll) {
	    Map<Integer, List<Vote>> votes = pollService.findVotes(poll);
	    long sum = votes.values().stream().collect(Collectors.summarizingInt(List::size)).getSum();
	    MessageBuilder s = MessageBuilder.build(MessageType.MARKDOWN)
			    .append(poll.getTitle()).append("\n")
			    .append("  von <").append(UserUtils.getName(event.getGuild(), poll.getCreatedBy()))
			    .append("> am ")
			    .append(DateTimeUtils.toString(poll.getCreated())).append("\n");
	    s.append("  ").append(sum).append(" Votes bisher\n");
		int index = 0;
	    for (String option : poll.getOptions()) {
		    s.append(voteToString(option, votes.get(index), index));
		    index++;
	    }
    	return s.toString();
    }

    @Override
    protected void execute(CommandEvent event) {
        String[] args = event.getArgs().split("\n");
        if(StringUtils.isBlank(event.getArgs())) {
	        Poll poll = pollService.findOpenByChannel(event.getTextChannel().getId());
	        if (poll == null) {
		        event.reply(MessageBuilder.build(MessageType.FORMATTED, "Gibt keine Umfrage").toString());
	        } else {
		        event.reply(pollToString(event, poll));
	        }
        } else if(args.length == 1) {
	        String[] modifier = event.getArgs().split("\\s+");
	        if(modifier.length == 1 && modifier[0].equalsIgnoreCase("close")) {
	        	pollService.close(event.getTextChannel().getId());
	        	event.reply(MessageBuilder.build(MessageType.FORMATTED, "Umfrage geschlossen").toString());
	        } else {
	        	event.reply(MessageBuilder.build(MessageType.FORMATTED, "Irgendwas haste falsch gemacht du Trottl").toString());
	        }
        } else if(args.length < 3) {
		    event.reply("Brauchst schon 2 Optionen für nen Poll du Mongo");
        } else {
            String title = args[0];
            String[] options = ArrayUtils.subarray(args, 1, args.length);
            event.getTextChannel().getId();
	        Poll poll = pollService.create(title, event.getAuthor().getId(), event.getChannel().getId(), options);
	        if(poll == null) {
	        	event.reply(MessageBuilder.build(MessageType.FORMATTED, "Gibt schon nen offenen Poll du Mongo").toString());
	        } else {
		        event.reply(pollToString(event, poll));
		        event.reply(MessageBuilder.build(MessageType.FORMATTED, "Poll erstellt!").toString());
	        }
        }
    }
}
