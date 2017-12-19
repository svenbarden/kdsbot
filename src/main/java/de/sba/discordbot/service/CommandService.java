package de.sba.discordbot.service;

import com.jagrosh.jdautilities.commandclient.Command;
import com.jagrosh.jdautilities.commandclient.CommandClientBuilder;
import net.dv8tion.jda.core.JDA;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class CommandService {
    @Autowired
    private JDA client;
    @Value("${de.sba.discordbot.ownerId}")
    private String ownerId;
    @Autowired
    private List<Command> commands;

    public CommandService() {
    }

    @PostConstruct
    private void init() {
        CommandClientBuilder commandClient = new CommandClientBuilder();
        commandClient.useDefaultGame();
        commandClient.setOwnerId(ownerId);
        commandClient.setPrefix("!");

        commands.forEach(commandClient::addCommand);
        client.addEventListener(commandClient.build());
    }
}
