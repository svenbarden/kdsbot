package de.sba.discordbot.service;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@org.springframework.context.annotation.Configuration
@ConfigurationProperties(prefix = "de.sba.discordbot")
public class Configuration {
    private List<String> nicNames = new ArrayList<>();
    private TopicConfiguration topic = new TopicConfiguration();

    public List<String> getNicNames() {
        return nicNames;
    }

    public TopicConfiguration getTopic() {
        return topic;
    }
}
