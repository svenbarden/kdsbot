package de.sba.discordbot;

import de.sba.discordbot.service.Configuration;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import javax.security.auth.login.LoginException;

@Service
public class BotService {
    private static final Logger LOGGER = LoggerFactory.getLogger(BotService.class);
    @Value("${de.sba.discordbot.token}")
    private String token;
    private JDA client;
    @Autowired
    private Configuration configuration;

    @Bean
    public JDA getClient() {
        LOGGER.info("starting client");
        String name = configuration.getNicNames().get(RandomUtils.nextInt(0, configuration.getNicNames().size()));
        if(client == null) {
            try {
                client = new JDABuilder(AccountType.BOT).setToken(token).buildBlocking();
            } catch (LoginException | InterruptedException | RateLimitedException e) {
                e.printStackTrace();
            }
        }
        client.getGuilds().forEach(guild -> LOGGER.info("found guild {} ({})", guild.getName(), guild.getId()));
        LOGGER.info("setting personal name to {}", name);
//        client.getSelfUser().getManagerUpdatable().getNameField().setValue(name);
        client.getSelfUser().getManager().setName(name).complete();
        return client;
    }
}
