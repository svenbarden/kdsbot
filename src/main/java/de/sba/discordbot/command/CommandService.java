package de.sba.discordbot.command;

import com.jagrosh.jdautilities.commandclient.CommandClientBuilder;
import de.sba.discordbot.PersistenceService;
import de.sba.discordbot.listener.GameLogService;
import net.dv8tion.jda.core.JDA;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class CommandService {
    private PersistenceService persistenceService;
    private GameLogService gameLogService;
    private JDA client;
    @Value("${de.sba.discordbot.ownerId}")
    private String ownerId;


    @Autowired
    public CommandService(PersistenceService persistenceService, GameLogService gameLogService, JDA client) {
        this.persistenceService = persistenceService;
        this.gameLogService = gameLogService;
        this.client = client;
    }

    @PostConstruct
    private void init() {
        CommandClientBuilder commandClient = new CommandClientBuilder();
        commandClient.useDefaultGame();
        commandClient.setOwnerId(ownerId);
        commandClient.setPrefix("!");

        commandClient.addCommand(new TopicCommand(persistenceService));
        commandClient.addCommand(new GameLogCommand(gameLogService));
        client.addEventListener(commandClient.build());
    }
}
