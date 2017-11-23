package de.sba.discordbot.service;

import com.jagrosh.jdautilities.commandclient.CommandClientBuilder;
import de.sba.discordbot.command.GameLogCommand;
import de.sba.discordbot.command.TopicCommand;
import net.dv8tion.jda.core.JDA;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class CommandService {
    private GameLogService gameLogService;
    private TopicService topicService;
    private JDA client;
    @Value("${de.sba.discordbot.ownerId}")
    private String ownerId;


    @Autowired
    public CommandService(GameLogService gameLogService, TopicService topicService, JDA client) {
        this.gameLogService = gameLogService;
        this.topicService = topicService;
        this.client = client;
    }

    @PostConstruct
    private void init() {
        CommandClientBuilder commandClient = new CommandClientBuilder();
        commandClient.useDefaultGame();
        commandClient.setOwnerId(ownerId);
        commandClient.setPrefix("!");

        commandClient.addCommand(new TopicCommand(topicService));
        commandClient.addCommand(new GameLogCommand(gameLogService));
        client.addEventListener(commandClient.build());
    }
}
